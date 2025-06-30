package com.cj.genieq.common.interceptor;

import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ⚠️ DEPRECATED: 이 클래스는 더 이상 사용되지 않습니다.
 * 
 * JWT 기반 인증 시스템으로 전환됨에 따라 세션 기반 AuthInterceptor는 사용 중단되었습니다.
 * 인증은 이제 Spring Security의 JwtAuthenticationFilter에서 처리됩니다.
 * 
 * @deprecated JWT 기반 인증 시스템으로 대체됨
 * @see com.cj.genieq.common.filter.JwtAuthenticationFilter
 */
@Deprecated
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ⚠️ 이 메서드는 더 이상 사용되지 않습니다.
        // JWT 인증은 JwtAuthenticationFilter에서 자동으로 처리됩니다.
        logger.warn("AuthInterceptor는 deprecated입니다. JWT 기반 인증을 사용하세요.");
        
        // 모든 요청을 통과시킴 (Spring Security에서 JWT 인증 처리)
        return true;
    }
}
