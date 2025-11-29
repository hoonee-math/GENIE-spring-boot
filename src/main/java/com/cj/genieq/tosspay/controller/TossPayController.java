package com.cj.genieq.tosspay.controller;

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
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;


@Slf4j
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
    public ResponseEntity<?> saveAmount(
            @AuthenticationPrincipal Long memCode,
            HttpSession session,
            @RequestBody TossPayRequestDto tossPayRequestDto) {

        try {
            // JWT로 인증된 사용자만 결제 초기화 가능
            log.info("결제 초기화 - memCode: {}", memCode);
            log.debug("결제 정보: {}", tossPayRequestDto);

            String orderId = tossPayRequestDto.getOrderId();
            String amount = tossPayRequestDto.getAmount();
            Long ticCode = tossPayRequestDto.getTicCode();

            // 결제 금액 세션 저장
            session.setAttribute(orderId + "_amount", amount);
            // ticCode 세션 저장
            session.setAttribute(orderId + "_ticCode", ticCode);
            // 보안을 위해 회원 코드도 세션에 저장하여 검증에 사용
            session.setAttribute(orderId + "_memberCode", memCode);

            return ResponseEntity.ok(Map.of(
                "message", "결제 임시 데이터 저장 성공", 
                "success", true,
                "orderId", orderId
            ));
            
        } catch (Exception e) {
            System.err.println("결제 임시 저장 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "message", "결제 데이터 저장에 실패했습니다.",
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    //결제 금액 검증
    @PostMapping("/verifyAmount")
    public ResponseEntity<?> verifyAmount(
            @AuthenticationPrincipal Long memCode,
            HttpSession session,
            @RequestBody TossPayRequestDto tossPayRequestDto) {
        // JWT로 인증된 사용자만 결제 검증 가능
        log.info("결제 검증 - memCode: {}", memCode);
        log.debug("검증 요청: {}", tossPayRequestDto);

        String orderId = tossPayRequestDto.getOrderId();

        // 세션에서 저장된 정보 조회
        String savedAmount = (String) session.getAttribute(orderId + "_amount");
        Long savedMemberCode = (Long) session.getAttribute(orderId + "_memberCode");

        // 결제 금액 검증
        if (savedAmount == null || !savedAmount.equals(tossPayRequestDto.getAmount())) {
            session.removeAttribute(orderId + "_amount");
            session.removeAttribute(orderId + "_memberCode");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message", "결제 금액 정보가 유효하지 않습니다.",
                            "success", false
                    ));
        }

        // 회원 코드 검증 (결제를 시작한 사용자와 검증하는 사용자가 같은지 확인)
        if (savedMemberCode == null || !savedMemberCode.equals(memCode)) {
            session.removeAttribute(orderId + "_amount");
            session.removeAttribute(orderId + "_memberCode");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message", "결제 권한이 없습니다.",
                            "success", false
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "결제 금액 검증이 완료되었습니다.",
                "success", true
        ));
    }

    //결제 승인
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            HttpSession session,
            @AuthenticationPrincipal Long memCode,
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

            // 2) 토스 응답 JSON 파싱
            JsonNode json = objectMapper.readTree(resp.body());
            String paymentMethod = json.get("method").asText();
            OffsetDateTime odtReq = OffsetDateTime.parse(json.get("requestedAt").asText());
            LocalDateTime requestedAt = odtReq.toLocalDateTime();

            OffsetDateTime odtApp = OffsetDateTime.parse(json.get("approvedAt").asText());
            LocalDateTime approvedAt = odtApp.toLocalDateTime();

            int totalAmount = json.get("totalAmount").asInt();

            // 3) 로그인 회원 검증
            // Spring Security가 자동으로 JWT 검증 및 사용자 정보 주입, 인증되지 않은 요청은 SecurityConfig에서 401 자동 처리

            // 4) 세션에서 결제 정보 꺼내고 즉시 제거
            String orderId = dto.getOrderId();
            Long ticCode = (Long) session.getAttribute(orderId + "_ticCode");
            Long savedMemberCode = (Long) session.getAttribute(orderId + "_memberCode");

            // 세션 정보 즉시 제거 (보안)
            session.removeAttribute(orderId + "_ticCode");
            session.removeAttribute(orderId + "_memberCode");

            // 5) 추가 보안 검증: 결제를 시작한 사용자와 승인하는 사용자가 같은지 확인
            if (savedMemberCode == null || !savedMemberCode.equals(memCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message", "결제 승인 권한이 없습니다.",
                                "success", false
                        ));
            }

            // 6) DB에 결제 내역 저장
            paymentService.insertPayment(
                    memCode,
                    ticCode,
                    dto.getOrderId(),
                    dto.getPaymentKey(),
                    paymentMethod,
                    requestedAt,
                    approvedAt,
                    Integer.valueOf(dto.getAmount())
            );

            System.out.println("결제 승인 및 DB 저장 완료");
            return ResponseEntity.ok(Map.of(
                    "message", "결제가 성공적으로 완료되었습니다.",
                    "success", true,
                    "paymentKey", dto.getPaymentKey(),
                    "orderId", dto.getOrderId(),
                    "amount", dto.getAmount()
            ));

        } catch (Exception e) {
            System.err.println("결제 승인 중 오류 - orderId: " + dto.getOrderId() + ", error: " + e.getMessage());

            // 실패 시 토스페이먼츠 결제 취소 시도
            try {
                requestCancel(dto.getPaymentKey(), Integer.parseInt(dto.getAmount()), "DB 처리 실패로 인한 자동 취소");
            } catch (Exception cancelException) {
                System.err.println("결제 취소 중 추가 오류: " + cancelException.getMessage());
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "결제 승인 중 오류가 발생했습니다.",
                "success", false,
                "error", e.getMessage()
            ));
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