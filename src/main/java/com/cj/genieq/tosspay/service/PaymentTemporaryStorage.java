package com.cj.genieq.tosspay.service;

import com.cj.genieq.tosspay.dto.PaymentTempData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Redis 기반 결제 임시 데이터 저장소
 * 
 * 기존 HttpSession 방식에서 Redis 기반으로 전환하여:
 * - 확장 가능한 분산 환경 지원
 * - 자동 만료 기능 (TTL)
 * - JWT 토큰 기반 인증과 일관성 유지
 * - 보안 강화 (memberCode 검증)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTemporaryStorage {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Redis 키 접두사
    private static final String PAYMENT_KEY_PREFIX = "payment:temp:";
    
    // 기본 만료 시간 (30분)
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    
    /**
     * 결제 임시 데이터 저장
     * 
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @param ticCode 티켓 코드
     * @param memberCode 회원 코드 (JWT에서 추출)
     * @return 저장된 데이터
     */
    public PaymentTempData savePaymentData(String orderId, String amount, Long ticCode, Long memberCode) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            PaymentTempData paymentData = PaymentTempData.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .ticCode(ticCode)
                    .memberCode(memberCode)
                    .createdAt(now)
                    .expiresAt(now.plus(DEFAULT_TTL))
                    .status("PENDING")
                    .build();
            
            String key = PAYMENT_KEY_PREFIX + orderId;
            
            // Redis에 저장 (자동 만료 설정)
            redisTemplate.opsForValue().set(key, paymentData, DEFAULT_TTL);
            
            log.info("결제 임시 데이터 저장 완료 - orderId: {}, memberCode: {}, amount: {}", 
                    orderId, memberCode, amount);
            
            return paymentData;
            
        } catch (Exception e) {
            log.error("결제 임시 데이터 저장 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            throw new RuntimeException("결제 데이터 저장에 실패했습니다.", e);
        }
    }
    
    /**
     * 결제 임시 데이터 조회
     * 
     * @param orderId 주문 ID
     * @return 저장된 결제 데이터 (없으면 Optional.empty())
     */
    public Optional<PaymentTempData> getPaymentData(String orderId) {
        try {
            String key = PAYMENT_KEY_PREFIX + orderId;
            Object data = redisTemplate.opsForValue().get(key);
            
            if (data == null) {
                log.warn("결제 임시 데이터 없음 - orderId: {}", orderId);
                return Optional.empty();
            }
            
            PaymentTempData paymentData = objectMapper.convertValue(data, PaymentTempData.class);
            log.debug("결제 임시 데이터 조회 성공 - orderId: {}, memberCode: {}", 
                    orderId, paymentData.getMemberCode());
            
            return Optional.of(paymentData);
            
        } catch (Exception e) {
            log.error("결제 임시 데이터 조회 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 결제 임시 데이터 삭제
     * 결제 완료 또는 실패 시 호출
     * 
     * @param orderId 주문 ID
     * @return 삭제 성공 여부
     */
    public boolean removePaymentData(String orderId) {
        try {
            String key = PAYMENT_KEY_PREFIX + orderId;
            Boolean deleted = redisTemplate.delete(key);
            
            if (Boolean.TRUE.equals(deleted)) {
                log.info("결제 임시 데이터 삭제 완료 - orderId: {}", orderId);
                return true;
            } else {
                log.warn("결제 임시 데이터 삭제 실패 (데이터 없음) - orderId: {}", orderId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("결제 임시 데이터 삭제 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 결제 데이터 검증
     * 기존 세션 방식의 verifyAmount 기능을 대체
     * 
     * @param orderId 주문 ID
     * @param amount 검증할 금액
     * @param memberCode 요청자 회원 코드
     * @return 검증 성공 여부
     */
    public boolean verifyPaymentData(String orderId, String amount, Long memberCode) {
        try {
            Optional<PaymentTempData> optionalData = getPaymentData(orderId);
            
            if (optionalData.isEmpty()) {
                log.warn("결제 검증 실패 - 데이터 없음: orderId={}", orderId);
                return false;
            }
            
            PaymentTempData data = optionalData.get();
            
            // 1. 금액 검증
            if (!amount.equals(data.getAmount())) {
                log.warn("결제 검증 실패 - 금액 불일치: orderId={}, expected={}, actual={}", 
                        orderId, data.getAmount(), amount);
                return false;
            }
            
            // 2. 회원 검증 (보안 강화)
            if (!memberCode.equals(data.getMemberCode())) {
                log.warn("결제 검증 실패 - 회원 불일치: orderId={}, expected={}, actual={}", 
                        orderId, data.getMemberCode(), memberCode);
                return false;
            }
            
            // 3. 만료 시간 검증
            if (LocalDateTime.now().isAfter(data.getExpiresAt())) {
                log.warn("결제 검증 실패 - 데이터 만료: orderId={}, expiresAt={}", 
                        orderId, data.getExpiresAt());
                removePaymentData(orderId); // 만료된 데이터 정리
                return false;
            }
            
            log.info("결제 데이터 검증 성공 - orderId: {}, memberCode: {}", orderId, memberCode);
            return true;
            
        } catch (Exception e) {
            log.error("결제 데이터 검증 중 오류 - orderId: {}, error: {}", orderId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 결제 상태 업데이트
     * 
     * @param orderId 주문 ID
     * @param status 새로운 상태
     * @return 업데이트 성공 여부
     */
    public boolean updatePaymentStatus(String orderId, String status) {
        try {
            Optional<PaymentTempData> optionalData = getPaymentData(orderId);
            
            if (optionalData.isEmpty()) {
                log.warn("결제 상태 업데이트 실패 - 데이터 없음: orderId={}", orderId);
                return false;
            }
            
            PaymentTempData data = optionalData.get();
            data.setStatus(status);
            
            String key = PAYMENT_KEY_PREFIX + orderId;
            
            // 남은 TTL 계산하여 재설정
            Duration remainingTTL = Duration.between(LocalDateTime.now(), data.getExpiresAt());
            if (remainingTTL.isNegative()) {
                remainingTTL = Duration.ofMinutes(5); // 최소 5분
            }
            
            redisTemplate.opsForValue().set(key, data, remainingTTL);
            
            log.info("결제 상태 업데이트 완료 - orderId: {}, status: {}", orderId, status);
            return true;
            
        } catch (Exception e) {
            log.error("결제 상태 업데이트 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Redis 연결 상태 확인
     * 헬스체크용
     * 
     * @return Redis 연결 상태
     */
    public boolean isRedisAvailable() {
        try {
            redisTemplate.opsForValue().set("health:check", "OK", Duration.ofSeconds(10));
            String result = (String) redisTemplate.opsForValue().get("health:check");
            return "OK".equals(result);
        } catch (Exception e) {
            log.error("Redis 연결 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}