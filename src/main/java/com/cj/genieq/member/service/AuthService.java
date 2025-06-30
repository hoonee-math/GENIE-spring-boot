package com.cj.genieq.member.service;

import com.cj.genieq.member.dto.request.SignUpRequestDto;
import com.cj.genieq.member.dto.response.LoginMemberResponseDto;

/**
 * 회원 인증 서비스 인터페이스
 * JWT 기반 인증으로 전환되면서 HttpSession 의존성 완전 제거
 */
public interface AuthService {
    
    // 중복 이메일 검사
    boolean checkEmailDuplicate(String email);

    // 회원가입
    void signUp(SignUpRequestDto signUpRequestDto);

    /**
     * 로그인 처리 및 JWT 토큰 발급
     * 기존 세션 기반에서 JWT 토큰 기반으로 전환
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return JWT 토큰이 포함된 로그인 응답 DTO
     */
    LoginMemberResponseDto login(String email, String password);

    /**
     * 회원탈퇴 (JWT 기반으로 전환)
     * @param email 탈퇴할 회원 이메일
     */
    void withdraw(String email);

    // 비밀번호 찾기
    void findPassword(String memEmail, String tempPassword);
}
