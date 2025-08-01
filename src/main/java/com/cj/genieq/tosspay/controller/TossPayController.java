package com.cj.genieq.tosspay.controller;

import com.cj.genieq.member.dto.AuthenticatedMemberDto;
import com.cj.genieq.payment.service.PaymentService;
import com.cj.genieq.tosspay.dto.PaymentTempData;
import com.cj.genieq.tosspay.dto.request.ConfirmPaymentRequestDto;
import com.cj.genieq.tosspay.dto.request.TossPayRequestDto;
import com.cj.genieq.tosspay.dto.request.TossWebhookPayload;
import com.cj.genieq.tosspay.dto.response.TossPayErrorResponse;
import com.cj.genieq.tosspay.service.PaymentTemporaryStorage;
import com.cj.genieq.tosspay.service.TossPayService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Map;


@RestController
@RequestMapping("/api/tosspay")
@RequiredArgsConstructor
public class TossPayController {
    private final ObjectMapper objectMapper;
    private final PaymentTemporaryStorage paymentTemporaryStorage;

    private static final String CONFIRM_URL =
            "https://api.tosspayments.com/v1/payments/confirm";
    private final TossPayService tossPayService;

    @Value("${tosspay.api.secret-key}")
    private String apiSecretKey;

    private final PaymentService paymentService;

    /**
     * 결제 금액 임시 저장 (Redis 기반)
     * 기존 HttpSession 방식에서 Redis 기반으로 전환
     * JWT 토큰에서 memberCode를 추출하여 보안 강화
     */
    @PostMapping("/saveAmount")
    public ResponseEntity<?> saveAmount(
            @AuthenticationPrincipal AuthenticatedMemberDto member,
            @RequestBody TossPayRequestDto tossPayRequestDto) {
        
        try {
            System.out.println("Redis 기반 결제 임시 저장 요청: " + tossPayRequestDto);
            
            String orderId = tossPayRequestDto.getOrderId();
            String amount = tossPayRequestDto.getAmount();
            Long ticCode = tossPayRequestDto.getTicCode();
            Long memberCode = member.getMemCode(); // JWT에서 추출한 회원 코드
            
            // Redis에 결제 임시 데이터 저장
            PaymentTempData savedData = paymentTemporaryStorage.savePaymentData(
                    orderId, amount, ticCode, memberCode
            );
            
            System.out.println("Redis 저장 완료 - orderId: " + orderId + ", memberCode: " + memberCode);
            
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

    /**
     * 결제 금액 검증 (Redis 기반)
     * 기존 HttpSession 방식에서 Redis 기반으로 전환
     * memberCode 추가 검증으로 보안 강화
     */
    @PostMapping("/verifyAmount")
    public ResponseEntity<?> verifyAmount(
            @AuthenticationPrincipal AuthenticatedMemberDto member,
            @RequestBody TossPayRequestDto tossPayRequestDto) {
        
        try {
            System.out.println("Redis 기반 결제 검증 요청: " + tossPayRequestDto);
            
            String orderId = tossPayRequestDto.getOrderId();
            String amount = tossPayRequestDto.getAmount();
            Long memberCode = member.getMemCode();
            
            // Redis에서 결제 데이터 검증
            boolean isValid = paymentTemporaryStorage.verifyPaymentData(orderId, amount, memberCode);
            
            if (!isValid) {
                System.out.println("결제 검증 실패 - orderId: " + orderId);
                return ResponseEntity.badRequest()
                        .body(TossPayErrorResponse.builder()
                                .code(400)
                                .message("결제 금액 정보가 유효하지 않습니다.")
                                .build());
            }
            
            System.out.println("결제 검증 성공 - orderId: " + orderId);
            return ResponseEntity.ok(Map.of(
                "message", "결제 데이터 검증 성공",
                "success", true,
                "orderId", orderId
            ));
            
        } catch (Exception e) {
            System.err.println("결제 검증 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "message", "결제 검증 중 오류가 발생했습니다.",
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * 결제 승인 (Redis 기반)
     * 기존 HttpSession 방식에서 Redis 기반으로 전환
     * 결제 완료 후 Redis 데이터 자동 정리
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            @AuthenticationPrincipal AuthenticatedMemberDto member,
            @RequestBody ConfirmPaymentRequestDto dto
    ) throws Exception {
        String orderId = dto.getOrderId();
        
        try {
            // 1) Redis에서 결제 데이터 조회
            var optionalPaymentData = paymentTemporaryStorage.getPaymentData(orderId);
            if (optionalPaymentData.isEmpty()) {
                System.err.println("결제 승인 실패 - Redis 데이터 없음: " + orderId);
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "결제 데이터를 찾을 수 없습니다.",
                    "success", false,
                    "orderId", orderId
                ));
            }
            
            PaymentTempData paymentData = optionalPaymentData.get();
            
            // 2) 회원 검증 (보안 강화)
            if (!member.getMemCode().equals(paymentData.getMemberCode())) {
                System.err.println("결제 승인 실패 - 회원 불일치: " + orderId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "결제 권한이 없습니다.",
                    "success", false,
                    "orderId", orderId
                ));
            }
            
            // 3) 결제 상태를 PROCESSING으로 업데이트
            paymentTemporaryStorage.updatePaymentStatus(orderId, "PROCESSING");
            
            // 4) 토스페이먼츠 승인 요청
            HttpResponse<String> resp = requestConfirm(dto);

            if (resp.statusCode() != 200) {
                // 실패 시 상태 업데이트
                paymentTemporaryStorage.updatePaymentStatus(orderId, "FAILED");
                return ResponseEntity
                        .status(resp.statusCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(resp.body());
            }

            // 5) 토스 응답 JSON 파싱
            JsonNode json = objectMapper.readTree(resp.body());
            String paymentMethod = json.get("method").asText();
            OffsetDateTime odtReq = OffsetDateTime.parse(json.get("requestedAt").asText());
            LocalDateTime requestedAt = odtReq.toLocalDateTime();

            OffsetDateTime odtApp = OffsetDateTime.parse(json.get("approvedAt").asText());
            LocalDateTime approvedAt = odtApp.toLocalDateTime();

            int totalAmount = json.get("totalAmount").asInt();

            // 6) DB에 결제 내역 저장
            paymentService.insertPayment(
                    member.getMemCode(),
                    paymentData.getTicCode(), // Redis에서 조회한 ticCode
                    dto.getOrderId(),
                    dto.getPaymentKey(),
                    paymentMethod,
                    requestedAt,
                    approvedAt,
                    Integer.valueOf(dto.getAmount())
            );

            // 7) 성공 시 Redis 데이터 정리
            paymentTemporaryStorage.removePaymentData(orderId);
            
            System.out.println("Redis 기반 결제 승인 성공 - orderId: " + orderId);
            return ResponseEntity.ok(Map.of(
                "message", "결제 성공",
                "success", true,
                "orderId", orderId,
                "paymentKey", dto.getPaymentKey()
            ));

        } catch (Exception e) {
            System.err.println("결제 승인 중 오류 - orderId: " + orderId + ", error: " + e.getMessage());
            
            // 실패 시 토스페이먼츠 결제 취소 및 Redis 상태 업데이트
            try {
                requestCancel(dto.getPaymentKey(), Integer.parseInt(dto.getAmount()), "DB 처리 실패로 인한 자동 취소");
                paymentTemporaryStorage.updatePaymentStatus(orderId, "FAILED");
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