package com.cj.genieq.tosspay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 결제 임시 데이터 DTO
 * Redis에 저장될 결제 관련 임시 정보
 * 기존 세션 방식에서 Redis 기반으로 전환하기 위한 데이터 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTempData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 주문 ID (토스페이먼츠에서 생성된 고유 식별자)
     */
    private String orderId;
    
    /**
     * 결제 금액
     */
    private String amount;
    
    /**
     * 티켓 코드 (이용권 종류)
     */
    private Long ticCode;
    
    /**
     * 회원 코드 (JWT 토큰에서 추출)
     * 보안을 위해 결제 데이터와 요청자 일치 검증용
     */
    private Long memberCode;
    
    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 만료 시간
     */
    private LocalDateTime expiresAt;
    
    /**
     * 결제 상태 (PENDING, PROCESSING, COMPLETED, FAILED, EXPIRED)
     */
    private String status;
    
    /**
     * 추가 메타데이터 (필요시 사용)
     */
    private String metadata;
}