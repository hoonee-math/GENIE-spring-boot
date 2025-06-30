package com.cj.genieq.common.jwt.util;

import com.cj.genieq.common.jwt.JwtTokenProvider;
import com.cj.genieq.common.jwt.dto.JwtTokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 관련 유틸리티 클래스
 * 토큰 생성과 응답 DTO 생성을 편리하게 처리
 */
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 액세스 토큰과 리프레시 토큰을 생성하여 JwtTokenDto로 반환
     * 로그인 성공 시 클라이언트에게 전달할 완전한 토큰 정보
     * @param memCode 사용자 ID (MemberEntity.memCode)
     * @param memEmail 사용자 이메일 (MemberEntity.memEmail)
     * @param role 사용자 권한
     * @return 토큰 쌍이 포함된 DTO
     */
    public JwtTokenDto createTokenPair(Long memCode, String memEmail, String role) {
        String accessToken = jwtTokenProvider.createAccessToken(memCode, memEmail, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(memCode);
        
        return JwtTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(3600L) // 1시간 (초 단위)
                .refreshTokenExpiresIn(604800L) // 7일 (초 단위)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 새로운 액세스 토큰만 생성하여 반환 (토큰 갱신용)
     * @param memCode 사용자 ID
     * @param memEmail 사용자 이메일
     * @param role 사용자 권한
     * @return 새로운 액세스 토큰
     */
    public String refreshAccessToken(Long memCode, String memEmail, String role) {
        return jwtTokenProvider.createAccessToken(memCode, memEmail, role);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * @param authorizationHeader Authorization 헤더 값
     * @return JWT 토큰 (Bearer 접두사 제거)
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * 토큰이 유효하고 만료되지 않았는지 확인
     * @param token JWT 토큰
     * @return 유효하고 만료되지 않았으면 true
     */
    public boolean isTokenValidAndNotExpired(String token) {
        return jwtTokenProvider.validateToken(token) && !jwtTokenProvider.isTokenExpired(token);
    }

    /**
     * 토큰에서 사용자 정보를 추출하여 Map으로 반환
     * @param token JWT 토큰
     * @return 사용자 정보 (memCode, memEmail, role)
     */
    public java.util.Map<String, Object> extractUserInfo(String token) {
        if (!isTokenValidAndNotExpired(token)) {
            return null;
        }
        
        java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("memCode", jwtTokenProvider.getMemberIdFromToken(token));
        userInfo.put("memEmail", jwtTokenProvider.getEmailFromToken(token));
        userInfo.put("role", jwtTokenProvider.getRoleFromToken(token));
        userInfo.put("tokenType", jwtTokenProvider.getTokenType(token));
        
        return userInfo;
    }
}
