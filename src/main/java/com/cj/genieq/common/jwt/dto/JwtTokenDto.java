package com.cj.genieq.common.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 토큰 쌍 (Access + Refresh) 응답 DTO
 * 로그인 성공 시 클라이언트에게 전달할 토큰 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenDto {
    
    // 액세스 토큰 (Authorization 헤더에 사용)
    private String accessToken;
    
    // 리프레시 토큰 (토큰 갱신에 사용)
    private String refreshToken;
    
    // 액세스 토큰 만료 시간 (초 단위)
    private Long accessTokenExpiresIn;
    
    // 리프레시 토큰 만료 시간 (초 단위)
    private Long refreshTokenExpiresIn;
    
    // 토큰 타입 (Bearer 고정)
    @Builder.Default
    private String tokenType = "Bearer";
}
