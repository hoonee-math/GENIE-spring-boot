package com.cj.genieq.common.exception;

/**
 * JWT 토큰 관련 예외 클래스
 * 토큰 검증, 파싱, 만료 등의 문제 발생 시 사용
 */
public class JwtAuthenticationException extends RuntimeException {
    
    public JwtAuthenticationException(String message) {
        super(message);
    }
    
    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
