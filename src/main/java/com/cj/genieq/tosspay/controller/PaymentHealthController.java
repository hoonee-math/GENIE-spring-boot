package com.cj.genieq.tosspay.controller;

import com.cj.genieq.tosspay.service.PaymentTemporaryStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 결제 시스템 헬스체크 컨트롤러
 * Redis 연결 상태 및 결제 임시 저장소 동작 확인용
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentHealthController {
    
    private final PaymentTemporaryStorage paymentTemporaryStorage;
    
    /**
     * Redis 연결 상태 확인
     * 개발/운영 환경에서 Redis 연결 테스트용
     */
    @GetMapping("/health/redis")
    public ResponseEntity<?> checkRedisHealth() {
        try {
            boolean isRedisAvailable = paymentTemporaryStorage.isRedisAvailable();
            
            if (isRedisAvailable) {
                return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "redis", "CONNECTED",
                    "message", "Redis 연결이 정상입니다."
                ));
            } else {
                return ResponseEntity.status(503).body(Map.of(
                    "status", "DOWN",
                    "redis", "DISCONNECTED",
                    "message", "Redis 연결에 실패했습니다."
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "redis", "ERROR",
                "message", "Redis 상태 확인 중 오류: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 결제 임시 저장소 기능 테스트
     * 개발 환경에서 Redis 기반 결제 시스템 동작 확인용
     */
    @GetMapping("/health/storage-test")
    public ResponseEntity<?> testPaymentStorage() {
        String testOrderId = "test_" + System.currentTimeMillis();
        
        try {
            // 1. 테스트 데이터 저장
            paymentTemporaryStorage.savePaymentData(testOrderId, "1000", 1L, 999L);
            
            // 2. 데이터 조회 테스트
            var retrievedData = paymentTemporaryStorage.getPaymentData(testOrderId);
            
            if (retrievedData.isEmpty()) {
                return ResponseEntity.status(500).body(Map.of(
                    "status", "FAIL",
                    "message", "데이터 저장 후 조회 실패"
                ));
            }
            
            // 3. 검증 테스트
            boolean isValid = paymentTemporaryStorage.verifyPaymentData(testOrderId, "1000", 999L);
            
            // 4. 데이터 정리
            paymentTemporaryStorage.removePaymentData(testOrderId);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Redis 기반 결제 임시 저장소 정상 동작",
                "testResult", Map.of(
                    "save", "OK",
                    "retrieve", "OK", 
                    "verify", isValid ? "OK" : "FAIL",
                    "cleanup", "OK"
                )
            ));
            
        } catch (Exception e) {
            // 실패 시에도 정리 시도
            try {
                paymentTemporaryStorage.removePaymentData(testOrderId);
            } catch (Exception cleanupException) {
                // 정리 실패는 무시
            }
            
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", "결제 저장소 테스트 실패: " + e.getMessage()
            ));
        }
    }
}