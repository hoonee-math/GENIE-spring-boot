package com.cj.genieq.common.config;

import com.cj.genieq.common.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security JWT 기반 설정
 * 
 * 기존 세션 기반 인증에서 JWT 토큰 기반 인증으로 완전 전환
 * - 세션 정책: STATELESS (JWT 토큰만 사용)
 * - 인증 필터: JwtAuthenticationFilter 사용
 * - CORS 설정: 통합 관리
 */
 @RequiredArgsConstructor
 @Configuration
 @EnableWebSecurity
 public class SecurityConfig {
 
     private final JwtAuthenticationFilter jwtAuthenticationFilter;
 
     @Bean
     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 기본 보안 설정
                .csrf(AbstractHttpConfigurer::disable) // REST API에서는 CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용으로 세션 비활성화
                
                // JWT 인증 필터 추가 (빈으로 주입받은 필터 사용)
                .addFilterBefore(jwtAuthenticationFilter, 
                    UsernamePasswordAuthenticationFilter.class)
                
                // 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                    // 인증 불필요 경로 (Public)
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", 
                                   "/swagger-resources/**", "/configuration/**").permitAll() // Swagger UI
                    .requestMatchers("/api/test/**").permitAll() // 테스트 API
                    .requestMatchers("/api/auth/select/login", "/api/auth/insert/signup", "/api/auth/refresh").permitAll() // 로그인/회원가입/토큰갱신
                    .requestMatchers("/api/auth/select/logout").permitAll() // 로그아웃 요청
                    .requestMatchers("/api/auth/select/email", "/api/auth/update/temporal").permitAll() // 이메일 확인/임시 비밀번호
                    .requestMatchers("/api/noti/**").permitAll() // 공지사항은 누구나 조회 가능
                    
                    // OAuth2 소셜 로그인 관련 경로 (향후 추가될 예정)
                    .requestMatchers("/oauth2/**", "/api/oauth2/**").permitAll()
                    
                    // 인증 필요 경로 (Protected) - JWT 토큰 필수
                    .requestMatchers("/api/info/**").authenticated() // 회원 정보
                    .requestMatchers("/api/tick/**").authenticated() // 이용권
                    .requestMatchers("/api/paym/**").authenticated() // 결제
                    .requestMatchers("/api/pass/**").authenticated() // 지문
                    .requestMatchers("/api/favo/**").authenticated() // 즐겨찾기
                    .requestMatchers("/api/ques/**").authenticated() // 문항
                    .requestMatchers("/api/usag/**").authenticated() // 이용내역
                    .requestMatchers("/api/tosspay/**").authenticated() // 토스 결제
                    
                    // 관리자 권한 필요 경로 (향후 확장)
                    // .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    
                    // 기타 모든 요청은 인증 필요
                    .anyRequest().authenticated()
                )
                
                // 예외 처리
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        System.out.println(authException.getMessage());
                        System.out.println(authException.getCause());
                        System.out.println(exceptions);
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                            "{\"error\":\"인증이 필요합니다. JWT 토큰을 확인해주세요.\",\"status\":401}"
                        );
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                            "{\"error\":\"접근 권한이 없습니다.\",\"status\":403}"
                        );
                    })
                )
                .build();
    }

    /**
     * 비밀번호 암호화 빈
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 설정 (WebMvcConfig에서 통합 이관)
     * 프론트엔드와의 교차 출처 요청을 처리
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 자격 증명 허용 (Authorization 헤더 포함)
        configuration.setAllowCredentials(true);
        
        // 허용할 출처
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173", // Vue 개발 서버
            "http://localhost:5174", // Vue 개발 서버 (대체)
            "http://127.0.0.1:5173",
            "http://localhost:80",
            "http://localhost:443",
            "http://localhost",
            "http://43.203.127.158", // 운영 서버
            "https://chunjae-it-edu.com", // 운영 도메인
            "https://genie.hoonee-math.info",
            "http://genie.hoonee-math.info" // GenieQ 운영 도메인 (HTTP)
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 허용할 헤더 (JWT Authorization 헤더 포함)
        configuration.setAllowedHeaders(List.of("*"));
        
        // 노출할 헤더
        configuration.setExposedHeaders(List.of(
            "Content-Disposition", // 파일 다운로드
            "Authorization" // JWT 토큰 응답
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
