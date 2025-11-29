package com.cj.genieq.common.filter;

import com.cj.genieq.common.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 토큰 인증 필터
 * Authorization 헤더의 JWT 토큰을 검증하고 Spring Security Context에 인증 정보 설정
 * Stateless JWT 인증: DB 조회 없이 토큰에서 memCode만 추출하여 Principal에 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // 현재 요청에 대한 인증 정보 로깅 (디버그용)
        logAuthenticationInfo(request);
        
        try {
            // Authorization 헤더에서 JWT 토큰 추출
            String token = getTokenFromRequest(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 토큰에서 사용자 ID 추출 (GENIE의 memCode)
                Long memCode = jwtTokenProvider.getMemberIdFromToken(token);

                // 토큰에서 권한 정보 추출
                String role = jwtTokenProvider.getRoleFromToken(token);

                // ROLE_ 접두사 추가
                if (role == null || role.trim().isEmpty()) {
                    role = "ROLE_USER"; // 기본 권한
                } else if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }

                // Spring Security 인증 객체 생성 (Principal = Long memCode)
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        memCode,  // Principal (사용자 ID만 저장)
                        null,     // Credentials (비밀번호 등, JWT에서는 불필요)
                        Collections.singletonList(new SimpleGrantedAuthority(role)) // 권한
                    );

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authentication successful for memCode: {}", memCode);

            } else if (token != null) {
                log.debug("Invalid JWT token received for request: {}", request.getRequestURI());
                clearSecurityContext();
            }
            // token이 null인 경우는 로그하지 않음 (공개 API 요청)

        } catch (Exception e) {
            log.error("JWT authentication failed for request: {} - Error: {}",
                    request.getRequestURI(), e.getMessage());
            clearSecurityContext();
        }
        
        // 다음 필터 실행 (인증 실패 시에도 계속 진행)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     * @return JWT 토큰 (없으면 null)
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 제거하고 토큰 부분만 반환
            return bearerToken.substring(7);
        }
        
        return null;
    }

    // 인증 실패 시 기존 인증 정보 제거 (SecurityContext 초기화)
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // JWT 인증이 불필요한 공개 API 경로들 (특정 경로에 대해 필터를 건너뛸지 결정)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // OPTIONS 요청은 CORS preflight이므로 JWT 검증 건너뛰기
        if ("OPTIONS".equals(method)) {
            return true;
        }
        
        // JWT 인증이 불필요한 경로들 (SecurityConfig의 permitAll과 일치)
        return path.startsWith("/swagger-ui/") ||           // Swagger UI
               path.startsWith("/v3/api-docs/") ||          // API 문서
               path.startsWith("/swagger-resources/") ||    // Swagger 리소스
               path.startsWith("/configuration/") ||        // Swagger 설정
               path.startsWith("/api/test/") ||             // 테스트 API
               path.equals("/api/auth/status") ||           // 인증 상태 확인
               path.equals("/api/auth/hello") ||            // 테스트 API
               path.equals("/api/auth/test-jwt") ||         // JWT 테스트
               path.equals("/api/auth/insert/signup") ||    // 회원가입
               path.equals("/api/auth/select/login") ||     // 로그인
               path.equals("/api/auth/refresh") ||          // 토큰 갱신
               path.equals("/api/auth/select/email") ||     // 이메일 확인
               path.equals("/api/auth/update/temporal") ||  // 임시 비밀번호
               path.startsWith("/oauth2/") ||               // OAuth2 관련 (향후 추가)
               path.startsWith("/login/") ||                // 로그인 관련 (향후 추가)
               path.equals("/") ||                          // 루트
               path.equals("/error");                       // 에러 페이지
    }

    /**
     * 현재 요청에 대한 인증 정보 로깅 (디버그용)
     * @param request HTTP 요청
     */
    private void logAuthenticationInfo(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            String token = getTokenFromRequest(request);
            log.debug("=== JWT Filter Debug Info ===");
            log.debug("Request URI: {}", request.getRequestURI());
            log.debug("Request Method: {}", request.getMethod());
            log.debug("Token Present: {}", token != null);
            log.debug("Should Filter: {}", !shouldNotFilter(request));
            
            if (token != null) {
                try {
                    Long memCode = jwtTokenProvider.getMemberIdFromToken(token);
                    log.debug("Token MemCode: {}", memCode);
                } catch (Exception e) {
                    log.debug("Token parsing failed: {}", e.getMessage());
                }
            }
            log.debug("============================");
        }
    }
}
