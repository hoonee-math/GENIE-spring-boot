package com.cj.genieq.member.controller;

import com.cj.genieq.member.dto.request.*;
import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import com.cj.genieq.member.dto.response.MemberInfoResponseDto;
import com.cj.genieq.member.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.cj.genieq.member.service.InfoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController //컨트롤러에서 반환된 값이 JSON 형태로 응답됨
@RequestMapping("/api")
@RequiredArgsConstructor //자동 생성자 주입
public class MemberController {

    private final AuthService authService;
    private final InfoService infoService;

    // Auth Controller

    // 회원가입 API
    @PostMapping("/auth/insert/signup")
    // ResponseEntity<?>를 사용하면 HTTP 상태 코드(200, 400, 401, 404, 500 등)와 함께 응답 데이터를 클라이언트에 명확하게 전달
    // @RequestBody는 클라이언트에서 받은 JSON 값을 객체로 매핑
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        // 이메일 중복 체크
        if (authService.checkEmailDuplicate(signUpRequestDto.getMemEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 이메일입니다.");
        }

        // 회원가입 처리
        authService.signUp(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");


    }

    /**
     * JWT 기반 로그인 API
     * 기존 세션 방식에서 JWT 토큰 방식으로 전환
     * 성공 시 JWT 토큰을 포함한 사용자 정보 반환
     */
    @PostMapping("/auth/select/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginMemberResponseDto loginResponse = authService.login(
                loginRequestDto.getMemEmail(), 
                loginRequestDto.getMemPassword()
            );
            return ResponseEntity.ok().body(loginResponse);
        } catch (IllegalArgumentException e) {
            // 로그인 실패 시 명확한 에러 메시지 반환
            return ResponseEntity.status(401).body(
                Map.of("error", e.getMessage(), "status", 401)
            );
        }
    }

    @PutMapping("/auth/remove/withdrawal")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequestDto withdrawRequestDto, HttpSession session){
        authService.withdraw(withdrawRequestDto.getMemEmail(), session);
        return ResponseEntity.ok("탈퇴완료");
    }

    @PostMapping("/auth/select/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 무효화
        }

        SecurityContextHolder.clearContext(); // Security 컨텍스트 삭제

        // JSESSIONID 쿠키 삭제
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().body("로그아웃 성공");
    }

    // Info Controller

    // 회원 정보 전체 조회
    @GetMapping("/info/select/entire")
    public ResponseEntity<?> selectEntire(HttpSession session){
        LoginMemberResponseDto loginMember = (LoginMemberResponseDto) session.getAttribute("LOGIN_USER");

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        } else {
            MemberInfoResponseDto memberInfo = infoService.getMemberInfo(loginMember.getMemberCode());

            return ResponseEntity.ok().body(memberInfo);
        }
    }

    // 회원의 잔여 이용권 조회
    @GetMapping("/info/select/ticket")
    public ResponseEntity<?> selectTicket(HttpSession session){
        LoginMemberResponseDto loginMember = (LoginMemberResponseDto) session.getAttribute("LOGIN_USER");

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        int balance = infoService.getUsageBalance(loginMember.getMemberCode());
        int total = infoService.getUsageTotal(loginMember.getMemberCode());

        Map<String, Integer> response = new HashMap<>();
        response.put("balance", balance);
        response.put("total",   total);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/info/update/name")
    public ResponseEntity<String> updateName(@RequestBody UpdateNameRequestDto updateNameRequestDto, HttpSession session){
        infoService.updateName(updateNameRequestDto.getMemName(), session);
        return ResponseEntity.ok("이름 수정 완료");
    }

    @PatchMapping("/info/update/type")
    public ResponseEntity<String> updateType(@RequestBody UpdateTypeRequestDto updateTypeRequestDto, HttpSession session){
        infoService.updateType(updateTypeRequestDto.getMemType(), session);
        return ResponseEntity.ok("소속 수정 완료");
    }

    @PatchMapping("/info/update/password")
    public ResponseEntity<String> updatePassword(
            @RequestBody UpdatePasswordRequestDto updatePasswordRequestDto,
            HttpSession session
            ){
        // System.out.println("start");
        infoService.updatePassword(
                updatePasswordRequestDto.getCurrentPassword(),
                updatePasswordRequestDto.getNewPassword(),
                updatePasswordRequestDto.getConfirmPassword(),
                session
        );
        // System.out.println("end");
        return ResponseEntity.ok("비밀번호 수정 완료");
    }

    @GetMapping("/auth/select/email")
    public ResponseEntity<String> checkEmail(@RequestParam String email){
        boolean isEmailExists = authService.checkEmailDuplicate(email);
        if (isEmailExists) {
            return ResponseEntity.ok("이메일이 존재합니다. 비밀번호 찾기를 진행합니다.");
        }
        return ResponseEntity.status(404).body("이메일이 존재하지 않습니다.");
    }

    @PutMapping("/auth/update/temporal")
    public ResponseEntity<String> findPassword(@RequestBody FindPasswordRequestDto findPasswordRequestDto){
        authService.findPassword(findPasswordRequestDto.getMemEmail(), findPasswordRequestDto.getTempPassword());
        return ResponseEntity.ok("임시 비밀번호가 성공적으로 업데이트되었습니다.");

    }
}
