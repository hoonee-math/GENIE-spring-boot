package com.cj.genieq.member.service;

import com.cj.genieq.member.dto.request.SignUpRequestDto;
import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import jakarta.servlet.http.HttpSession;

public interface AuthService {
    // 중복 이메일 검사
    boolean checkEmailDuplicate(String email);

    // 회원가입
    void signUp(SignUpRequestDto signUpRequestDto);

    // 로그인 처리 및 세션 저장 메서드
    LoginMemberResponseDto login(String email, String password, HttpSession session);

    //회원탈퇴
    void withdraw(String email, HttpSession session);

    //비밀번호 찾기
    void findPassword(String memEmail, String tempPassword);

}
