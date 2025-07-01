package com.cj.genieq.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO (보안 우선 httpOnly 쿠키 방식)
 * access token은 응답으로, refresh token은 httpOnly 쿠키로 관리
 * XSS 공격 완전 차단을 위한 보안 강화 설계
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class LoginMemberResponseDto {
    
    // ========== 기존 GENIE 필드 ==========
    private Long memberCode;
    private String name;
    private String email;
    
    // ========== JWT 토큰 관련 필드 (보안 강화) ==========
    
    /**
     * 액세스 토큰 (JWT)
     * 짧은 만료시간(15분)으로 보안 강화
     * 프론트엔드 메모리에서만 관리
     */
    private String accessToken;
    
    /**
     * 토큰 타입 (고정값: "Bearer")
     * Authorization 헤더 설정용
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * 액세스 토큰 만료 시간 (Unix timestamp, milliseconds)
     * 자동 토큰 갱신 타이밍 계산용
     */
    private Long expiresAt;
    
    // refresh token은 httpOnly 쿠키로 관리하므로 응답에 포함하지 않음
    // 보안 강화: JavaScript로 접근 불가
}
