package com.cj.genieq.common.interceptor;

import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그: 인증 인터셉터 실행
        logger.info("AuthInterceptor 실행 - 요청 경로: {}", request.getRequestURI());

        // OPTIONS 요청은 CORS preflight 요청이므로 통과
        if (request.getMethod().equals("OPTIONS")) {
            logger.info("OPTIONS 요청 통과");
            return true;
        }

        // 세션 확인
        HttpSession session = request.getSession(false);

        // 세션이 없거나 로그인 정보가 없는 경우
        if (session == null || session.getAttribute("LOGIN_USER") == null) {
            logger.warn("인증 실패 - 세션 없음 또는 로그인 정보 없음");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"인증이 필요합니다.\",\"status\":401}");
            return false;
        }

        // 로그인 상태 확인
        LoginMemberResponseDto loginUser = (LoginMemberResponseDto) session.getAttribute("LOGIN_USER");
        logger.info("인증 성공 - 사용자: {}", loginUser.getEmail());

        return true;
    }
}