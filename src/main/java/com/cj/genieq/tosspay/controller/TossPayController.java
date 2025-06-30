package com.cj.genieq.tosspay.controller;

import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.payment.service.PaymentService;
import com.cj.genieq.tosspay.dto.request.ConfirmPaymentRequestDto;
import com.cj.genieq.tosspay.dto.request.TossPayRequestDto;
import com.cj.genieq.tosspay.dto.request.TossWebhookPayload;
import com.cj.genieq.tosspay.dto.response.TossPayErrorResponse;
import com.cj.genieq.tosspay.service.TossPayService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;


@RestController
@RequestMapping("/api/tosspay")
@RequiredArgsConstructor
public class TossPayController {
    private final ObjectMapper objectMapper;

    private static final String CONFIRM_URL =
            "https://api.tosspayments.com/v1/payments/confirm";
    private final TossPayService tossPayService;

    @Value("${tosspay.api.secret-key}")
    private String apiSecretKey;

    private final PaymentService paymentService;

    //결제 금액 임시 저장
    @PostMapping("/saveAmount")
    public ResponseEntity<?> saveAmount(HttpSession session,@RequestBody TossPayRequestDto tossPayRequestDto) {
        System.out.println("토스 검증시 필요한 세션 저장용: "+tossPayRequestDto);
        String orderId = tossPayRequestDto.getOrderId();
        String amount = tossPayRequestDto.getAmount();
        Long ticCode = tossPayRequestDto.getTicCode();

        // 결제 금액 세션 저장
        session.setAttribute(orderId + "_amount", amount);
        // ticCode 세션 저장
        session.setAttribute(orderId + "_ticCode", ticCode);

        return ResponseEntity.ok("Payment temp save successful");
    }

    //결제 금액 검증
    @PostMapping("/verifyAmount")
    public ResponseEntity<?> verifyAmount(HttpSession session,@RequestBody TossPayRequestDto tossPayRequestDto) {
        System.out.println(tossPayRequestDto);

        String orderId = tossPayRequestDto.getOrderId();

        String saved = (String) session.getAttribute(orderId + "_amount");

        if (saved == null || !saved.equals(tossPayRequestDto.getAmount())) {
            // 검증 성공 → 세션에서 제거
            session.removeAttribute(orderId + "_amount");
            return ResponseEntity
                    .badRequest()
                    .body(TossPayErrorResponse.builder()
                            .code(400)
                            .message("결제 금액 정보가 유효하지 않습니다.")
                            .build());
        }

        return ResponseEntity.ok("Payment is valid");
    }

    //결제 승인
    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(
            HttpSession session,
            @AuthenticationPrincipal MemberEntity member,
            @RequestBody ConfirmPaymentRequestDto dto
    ) throws Exception {
        try {
            // 1) 토스페이먼츠 승인 요청
            HttpResponse<String> resp = requestConfirm(dto);

            if (resp.statusCode() != 200) {
                //상태 코드 그대로 전달, body 문자열(JSON)도 그대로 넘김
                return ResponseEntity
                        .status(resp.statusCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(resp.body());
            }

            // 2) 토스 응답 JSON 파싱 (Jackson)
            JsonNode json = objectMapper.readTree(resp.body());
            String paymentMethod   = json.get("method").asText();
            OffsetDateTime odtReq  = OffsetDateTime.parse(json.get("requestedAt").asText());
            LocalDateTime requestedAt = odtReq.toLocalDateTime();

            OffsetDateTime odtApp  = OffsetDateTime.parse(json.get("approvedAt").asText());
            LocalDateTime approvedAt  = odtApp.toLocalDateTime();

            int totalAmount = json.get("totalAmount").asInt();

            // 3) 로그인 회원 검증
            // Spring Security가 자동으로 JWT 검증 및 사용자 정보 주입, 인증되지 않은 요청은 SecurityConfig에서 401 자동 처리

            // 4) 세션에서 ticCode 꺼내고 즉시 제거
            String orderId = dto.getOrderId();
            Long ticCode = (Long) session.getAttribute(orderId + "_ticCode");
            session.removeAttribute(orderId + "_ticCode");

            // 5) DB에 결제 내역 저장
            paymentService.insertPayment(
                    member.getMemCode(),
                    ticCode,
                    dto.getOrderId(),
                    dto.getPaymentKey(),
                    paymentMethod,
                    requestedAt,
                    approvedAt,
                    Integer.valueOf(dto.getAmount())
            );

            System.out.println("성공");
            return ResponseEntity.ok().body("결제 성공");

        } catch (Exception e) {
            requestCancel(dto.getPaymentKey(), Integer.parseInt(dto.getAmount()), "DB 처리 실패로 인한 자동 취소");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"결제 승인 중 오류가 발생했습니다.\"}");
        }
    }

    //토스에게 결제 승인 요청
    private HttpResponse<String> requestConfirm(ConfirmPaymentRequestDto dto)
            throws Exception {

        // 1) 요청으로 받은 필드
        String orderId    = dto.getOrderId();
        String amount     = dto.getAmount();
        String paymentKey = dto.getPaymentKey();

        // 2) JSON 바디 생성
        ObjectNode bodyNode = objectMapper.createObjectNode()
                .put("paymentKey", paymentKey)
                .put("orderId",    orderId)
                .put("amount",     Integer.parseInt(amount));
        String requestBody = objectMapper.writeValueAsString(bodyNode);

        // 3) Basic 인증 헤더 생성
        String credential = Base64.getEncoder()
                .encodeToString((apiSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        // 4) HttpRequest 빌드
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CONFIRM_URL))
                .header("Authorization", "Basic " + credential)
                .header("Content-Type",  "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 5) 전송 및 응답 받기
        return HttpClient
                .newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    //토스에게 결제 취소하기
    private HttpResponse<String> requestCancel(String paymentKey, int cancelAmount, String reason)
            throws Exception {

        // 1) JSON 바디 생성
        ObjectNode bodyNode = objectMapper.createObjectNode()
                .put("cancelReason", reason)
                .put("cancelAmount", cancelAmount);

        String requestBody = objectMapper.writeValueAsString(bodyNode);

        // 2) 인증 헤더 생성
        String credential = Base64.getEncoder()
                .encodeToString((apiSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        // 3) HttpRequest 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel"))
                .header("Authorization", "Basic " + credential)
                .header("Content-Type",  "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 4) 요청 전송 및 응답 받기
        return HttpClient
                .newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    //토스 웹훅
    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String raw,
            @RequestHeader("x-toss-timestamp") String ts,
            @RequestHeader("x-toss-signature")  String sig
    ) throws JsonProcessingException {

        TossWebhookPayload payload =
                new ObjectMapper().readValue(raw, TossWebhookPayload.class);

        tossPayService.process(payload);

        return ResponseEntity.ok("OK");
    }

}