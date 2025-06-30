package com.cj.genieq.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 * 
 * 이전에 포함되었던 CORS 설정과 AuthInterceptor는 제거되었습니다:
 * - CORS 설정: SecurityConfig로 통합 이관됨
 * - AuthInterceptor: JWT 기반 인증으로 대체됨 (JwtAuthenticationFilter 사용)
 * 
 * 현재는 기본 MVC 설정만 유지하며, 필요시 추가 설정을 여기에 구현할 수 있습니다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    // 🎯 현재는 기본 설정만 사용
    // 향후 필요한 MVC 관련 설정(예: 메시지 컨버터, 뷰 리졸버 등)은 여기에 추가
    
}
