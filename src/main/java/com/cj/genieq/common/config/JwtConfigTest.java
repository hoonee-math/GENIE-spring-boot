package com.cj.genieq.common.config;

import com.cj.genieq.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * JWT 설정과 JwtTokenProvider가 정상적으로 동작하는지 확인하기 위한 검증 클래스
 * 태스크 2에서 JwtTokenProvider 구현 완료 후 업데이트됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test") // 테스트 환경에서는 실행하지 않음
public class JwtConfigTest implements CommandLineRunner {

    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${jwt.secret:NOT_FOUND}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-validity:0}")
    private long accessTokenValidity;
    
    @Value("${jwt.refresh-token-validity:0}")
    private long refreshTokenValidity;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== JWT 설정 검증 ===");
        log.info("JWT Secret 로딩: {}", jwtSecret.equals("NOT_FOUND") ? "❌ 실패" : "✅ 성공");
        log.info("Access Token Validity: {} ms ({}시간)", accessTokenValidity, accessTokenValidity / (1000 * 60 * 60));
        log.info("Refresh Token Validity: {} ms ({}일)", refreshTokenValidity, refreshTokenValidity / (1000 * 60 * 60 * 24));
        
        // 비밀키 길이 검증 (최소 64자)
        if (!jwtSecret.equals("NOT_FOUND") && jwtSecret.length() >= 64) {
            log.info("JWT Secret 길이: ✅ {}자 (64자 이상 권장)", jwtSecret.length());
        } else {
            log.warn("JWT Secret 길이: ❌ {}자 (64자 이상 필요)", jwtSecret.length());
        }
        
        log.info("=== JWT 의존성 확인 ===");
        try {
            Class.forName("io.jsonwebtoken.Jwts");
            log.info("JJWT 라이브러리: ✅ 정상 로딩");
        } catch (ClassNotFoundException e) {
            log.error("JJWT 라이브러리: ❌ 로딩 실패");
        }
        
        try {
            Class.forName("org.springframework.security.oauth2.client.registration.ClientRegistration");
            log.info("OAuth2 Client 라이브러리: ✅ 정상 로딩");
        } catch (ClassNotFoundException e) {
            log.error("OAuth2 Client 라이브러리: ❌ 로딩 실패");
        }
        
        log.info("=== JwtTokenProvider 동작 테스트 ===");
        try {
            // 테스트용 토큰 생성
            Long testMemCode = 1L;
            String testEmail = "test@genieq.com";
            String testRole = "ROLE_USER";
            
            String accessToken = jwtTokenProvider.createAccessToken(testMemCode, testEmail, testRole);
            String refreshToken = jwtTokenProvider.createRefreshToken(testMemCode);
            
            log.info("토큰 생성: ✅ 성공");
            
            // 토큰 검증
            boolean accessValid = jwtTokenProvider.validateToken(accessToken);
            boolean refreshValid = jwtTokenProvider.validateToken(refreshToken);
            
            log.info("토큰 검증: {} (Access: {}, Refresh: {})", 
                    (accessValid && refreshValid) ? "✅ 성공" : "❌ 실패", 
                    accessValid ? "✅" : "❌", 
                    refreshValid ? "✅" : "❌");
            
            // 토큰에서 정보 추출 테스트
            Long extractedMemCode = jwtTokenProvider.getMemberIdFromToken(accessToken);
            String extractedEmail = jwtTokenProvider.getEmailFromToken(accessToken);
            String extractedRole = jwtTokenProvider.getRoleFromToken(accessToken);
            
            boolean dataIntegrity = testMemCode.equals(extractedMemCode) && 
                                  testEmail.equals(extractedEmail) && 
                                  testRole.equals(extractedRole);
            
            log.info("데이터 무결성: {} (MemCode: {}, Email: {}, Role: {})", 
                    dataIntegrity ? "✅ 성공" : "❌ 실패",
                    testMemCode.equals(extractedMemCode) ? "✅" : "❌",
                    testEmail.equals(extractedEmail) ? "✅" : "❌",
                    testRole.equals(extractedRole) ? "✅" : "❌");
            
            if (accessValid && refreshValid && dataIntegrity) {
                log.info("=== 태스크 2 완료: JWT 토큰 제공자 서비스 구현 ✅ ===");
                
                // 태스크 3: JWT 인증 필터 확인
                log.info("=== JWT 인증 필터 확인 ===");
                try {
                    Class.forName("com.cj.genieq.common.filter.JwtAuthenticationFilter");
                    log.info("JwtAuthenticationFilter 클래스: ✅ 생성 완료");
                    
                    Class.forName("com.cj.genieq.common.auth.AuthenticationUtil");
                    log.info("AuthenticationUtil 클래스: ✅ 생성 완료");
                    
                    log.info("=== 태스크 3 완료: JWT 인증 필터 구현 ✅ ===");
                    
                } catch (ClassNotFoundException e) {
                    log.error("JWT 인증 필터 클래스 로딩 실패: {}", e.getMessage());
                    log.error("=== 태스크 3 검증 실패: JWT 인증 필터 오류 ❌ ===");
                }
            } else {
                log.error("=== 태스크 2 검증 실패: JWT 토큰 제공자 서비스 문제 발생 ❌ ===");
            }
            
        } catch (Exception e) {
            log.error("JwtTokenProvider 동작 테스트 실패: {}", e.getMessage());
            log.error("=== 태스크 2 검증 실패: JWT 토큰 제공자 서비스 오류 ❌ ===");
        }
    }
}
