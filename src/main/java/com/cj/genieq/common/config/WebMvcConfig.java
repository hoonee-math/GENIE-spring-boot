package com.cj.genieq.common.config;

import com.cj.genieq.common.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    // REST API 백엔드 구현을 위한 중요한 설정 파일
    // CORS 설정, 컨트롤러 요청 매핑 및 변환 설정, 인터셉터 등록

    private final AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins( "http://localhost:5173","http://localhost:5174","http://localhost:80","http://localhost","http://localhost:443","https://chunjae-it-edu.com")// Vue 프로젝트의 개발 서버 주소
                .allowedMethods("GET", "POST", "PUT", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition") // 파일 이름 추출 허용
                .allowCredentials(true);
    }

    public WebMvcConfig() {
        // 직접 인스턴스 생성하여 할당
        this.authInterceptor = new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**", "/tick/**", "/paym/**", "/subj/**", "/pass/**",
                        "/favo/**", "/form/**", "/ques/**", "/noti/**") // 인증이 필요한 경로
                .excludePathPatterns("/api/auth/select/login", "/api/auth/insert/signup",
                        "/api/auth/select/email", "/api/auth/update/temporal",
                        "/swagger-ui/**", "/v3/api-docs/**", "/api/test/**"); // 인증이 필요 없는 경로
    }
}
