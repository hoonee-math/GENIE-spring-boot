package com.cj.genieq.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 * 기존 세션 기반 응답에 JWT 토큰 정보 추가
 * 프론트엔드 호환성을 위해 기존 필드 유지
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class LoginMemberResponseDto {
    
    // ========== 기존 GENIE 필드 (JWT 전환 전부터 사용) ==========
    private Long memberCode;
    private String name;
    private String email;
    
    // ========== JWT 토큰 관련 필드 (JWT 전환 후 추가) ==========
    
    /**
     * 액세스 토큰 (JWT)
     * API 요청 시 Authorization 헤더에 포함하여 전송
     * 유효기간: 1시간 (기본값)
     */
    private String accessToken;
    
    /**
     * 리프레시 토큰 (향후 개선 예정)
     * 액세스 토큰 만료 시 새로운 토큰 발급용
     * 현재는 null로 설정 (향후 구현 예정)
     */
    private String refreshToken;
    
    /**
     * 토큰 타입 (고정값: "Bearer")
     * 프론트엔드에서 Authorization 헤더 설정 시 사용
     * 예: Authorization: Bearer eyJ...
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * 토큰 만료 시간 (Unix timestamp, milliseconds)
     * 프론트엔드에서 토큰 만료 시점 확인용
     */
    private Long expiresAt;
}
