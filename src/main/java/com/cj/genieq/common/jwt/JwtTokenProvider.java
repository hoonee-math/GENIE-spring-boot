package com.cj.genieq.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성, 검증, 파싱을 담당하는 서비스
 * GENIE 프로젝트용으로 PLANA의 JwtTokenProvider를 이식하여 구현
 * 액세스 토큰과 리프레시 토큰을 모두 처리
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // JWT 서명에 사용할 비밀키 (application-private.properties에서 주입)
    private final SecretKey secretKey;

    // @return 액세스 토큰 유효기간 (밀리초)
    @Getter
    private final long accessTokenValidityInMilliseconds;

    // 리프레시 토큰 만료시간 (기본: 7일)
    @Getter
    private final long refreshTokenValidityInMilliseconds;

    /**
     * JwtTokenProvider 생성자
     * @param secretKey JWT 서명용 비밀키 (64자 이상 필수)
     * @param accessTokenValidityInMilliseconds 액세스 토큰 유효기간 (밀리초)
     * @param refreshTokenValidityInMilliseconds 리프레시 토큰 유효기간 (밀리초)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity:900000}") long accessTokenValidityInMilliseconds, // 15분으로 변경 (900초 = 900000ms)
            @Value("${jwt.refresh-token-validity:604800000}") long refreshTokenValidityInMilliseconds) { // 7일 유지
        
        // 비밀키 유효성 검증 (반드시 application-private.properties에 설정 필요)
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key must be provided in application-private.properties");
        }
        
        if (secretKey.length() < 64) {
            throw new IllegalArgumentException("JWT secret key must be at least 64 characters long");
        }
        
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
        
        log.info("JwtTokenProvider 초기화 완료 - Access Token: {}ms, Refresh Token: {}ms", 
                accessTokenValidityInMilliseconds, refreshTokenValidityInMilliseconds);
    }

    // @return JWT 액세스 토큰 생성 (Stateless JWT - 최소 정보만 포함)
    public String createAccessToken(Long memCode, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(memCode.toString()) // 토큰 주체 (사용자 ID)
                .claim("role", role) // 사용자 권한
                .claim("type", "access") // 토큰 타입
                .setIssuedAt(now) // 발급 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS512) // 서명
                .compact();
    }

    // @return JWT 리프레시 토큰 생성
    public String createRefreshToken(Long memCode) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(memCode.toString()) // 토큰 주체 (사용자 ID)
                .claim("type", "refresh") // 토큰 타입
                .setIssuedAt(now) // 발급 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS512) // 서명
                .compact();
    }

    // @return 사용자 ID (memCode)
    public Long getMemberIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }

    // @return 사용자 권한
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    // 토큰 유효성 검증, @return 유효하면 true, 아니면 false
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 Claims 추출
     * @param token JWT 토큰
     * @return Claims 객체
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // @return 토큰 만료 시간
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    // @return 토큰이 만료되었으면 true, 아니면 false
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // 토큰이 유효하지 않으면 만료된 것으로 간주
        }
    }

    // @return 토큰 타입 (access 또는 refresh)
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("type", String.class);
    }
}
