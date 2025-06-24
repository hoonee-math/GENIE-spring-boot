package com.cj.genieq.common.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 기능 비활성화 (REST API에서는 일반적으로 비활성화)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // 요청에 대한 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/configuration/**").permitAll() // Swagger 허용
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll() // API 요청에 대한 접근 허용
                        .requestMatchers("/api/auth/**").permitAll() // 회원 인증
                        .requestMatchers("/api/info/**").permitAll() // 회원 정보
                        .requestMatchers("/api/tick/**").permitAll() // 이용권
                        .requestMatchers("/api/paym/**").permitAll() // 결제
                        .requestMatchers("/api/subj/**").permitAll() // 지문주제
                        .requestMatchers("/api/pass/**").permitAll() // 지문
                        .requestMatchers("/api/favo/**").permitAll() // 즐겨찾기
                        .requestMatchers("/api/form/**").permitAll() // 문항형식
                        .requestMatchers("/api/ques/**").permitAll() // 문항
                        .requestMatchers("/api/noti/**").permitAll() // 공지
                        .requestMatchers("/api/usag/**").permitAll() // 이용내역
                        .requestMatchers("/api/tosspay/**").permitAll() // 토스 결제
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true); // 자격 증명 허용 (세션, 쿠키 등)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173", "http://43.203.127.158","http://localhost:5174","http://localhost:80","http://localhost:443","http://localhost", "https://chunjae-it-edu.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // 허용할 메서드 설정
        configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
