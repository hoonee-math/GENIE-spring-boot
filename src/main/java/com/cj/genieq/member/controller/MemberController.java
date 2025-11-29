package com.cj.genieq.member.controller;

import com.cj.genieq.member.dto.request.*;
import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import com.cj.genieq.member.dto.response.MemberInfoResponseDto;
import com.cj.genieq.member.service.AuthService;
import com.cj.genieq.member.service.InfoService;
import com.cj.genieq.common.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController //컨트롤러에서 반환된 값이 JSON 형태로 응답됨
@RequestMapping("/api")
@RequiredArgsConstructor //자동 생성자 주입
public class MemberController {

    private final AuthService authService;
    private final InfoService infoService;
    private final JwtTokenProvider jwtTokenProvider;

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
     * JWT 기반 로그인 API (보안 우선 httpOnly 쿠키 방식)
     * access token은 응답으로, refresh token은 httpOnly 쿠키로 관리
     * XSS 공격 완전 차단을 위한 보안 강화 설계
     */
    @PostMapping("/auth/select/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        System.out.println("/api/auth/select/login 로그인 요청 들어옴");
        try {
            System.out.println("로그인 요청 email: "+loginRequestDto.getMemEmail());
            // 1. 기본 로그인 처리 (AuthService에서 사용자 정보만 반환)
            LoginMemberResponseDto loginResponse = authService.login(
                loginRequestDto.getMemEmail(), 
                loginRequestDto.getMemPassword()
            );
            System.out.println("DB에서 확인 후 loginResponseDTO 정보: "+loginResponse.toString());

            // 2. refresh token 생성 및 httpOnly 쿠키 설정
            String refreshToken = jwtTokenProvider.createRefreshToken(loginResponse.getMemberCode());
            System.out.println("리프레시 토큰 생성 결과 refreshToken: "+refreshToken);
            
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);  // XSS 방어: JavaScript 접근 차단
            refreshCookie.setSecure(true);   // HTTPS에서만 전송 // ?? 개발환경: HTTP 허용, 운영환경에서는 true로 변경 필요
            refreshCookie.setPath("/");       // 모든 경로에서 유효
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            refreshCookie.setAttribute("SameSite", "strict"); // CSRF 방어 // ?? 개발환경: Lax, 운영환경에서는 Strict ?
            System.out.println("최종 리프레스 쿠기 정보: "+refreshCookie);
            
            response.addCookie(refreshCookie);
            
            // 3. access token만 응답으로 전송 (refresh token은 응답에서 제외)
            return ResponseEntity.ok().body(loginResponse);
            
        } catch (IllegalArgumentException e) {
            // 로그인 실패 시 명확한 에러 메시지 반환
            return ResponseEntity.status(401).body(
                Map.of("error", e.getMessage(), "status", 401)
            );
        }
    }

    /**
     * 회원탈퇴 API (JWT 기반)
     * 기존 세션 방식에서 JWT 토큰 기반 인증으로 전환
     */
    @PutMapping("/auth/remove/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequestDto withdrawRequestDto, @AuthenticationPrincipal Long memCode) {
        // JWT 인증으로 본인 확인 완료 (SecurityContext에 memCode 저장됨)
        log.info("회원탈퇴 요청 - memCode: {}", memCode);

        try {
            // 이메일 대신 memCode로 탈퇴 처리
            authService.withdraw(withdrawRequestDto.getMemEmail());
            // 3. 성공 로그
            log.info("회원탈퇴 완료 - memCode: {}", memCode);
            return ResponseEntity.ok(Map.of("message", "탈퇴완료", "status", 200));

        } catch (Exception e) {
            // 4. 실패 로그
            log.error("회원탈퇴 실패 - memCode: {}, 오류: {}", memCode, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "탈퇴 처리 중 오류가 발생했습니다.", "status", 500));
        }
    }
    
    /**
     * JWT 토큰 갱신 API (보안 우선 httpOnly 쿠키 방식)
     * httpOnly 쿠키의 refresh token으로 새로운 access token 발급
     * 페이지 새로고침 시 자동 호출되는 느낌으로 설계
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        System.out.println("/api/auth/refresh 로 요청 들어옴");
        try {
            // 1. httpOnly 쿠키에서 refresh token 추출
            Cookie[] cookies = request.getCookies();
            String refreshToken = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }
            
            // 2. refresh token 유효성 검증
            if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.status(401).body(
                    Map.of("error", "Invalid or missing refresh token", "status", 401)
                );
            }
            
            // 3. refresh token 타입 확인
            if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
                return ResponseEntity.status(401).body(
                    Map.of("error", "Invalid token type", "status", 401)
                );
            }
            
            // 4. 사용자 정보 추출 및 새로운 access token 생성
            Long memberCode = jwtTokenProvider.getMemberIdFromToken(refreshToken);

            // DB에서 사용자 정보 조회하여 실제 정보로 토큰 생성
            MemberInfoResponseDto memberInfo = infoService.getMemberInfo(memberCode);

            
            String newAccessToken = jwtTokenProvider.createAccessToken(
                memberCode, memberInfo.getEmail(), "USER" // 기본 권한으로 설정 or memberInfo.getType()
            );
            
            // 5. 로그인과 동일한 구조로 응답 (LoginMemberResponseDto 사용)
            LoginMemberResponseDto response = LoginMemberResponseDto.builder()
                .memberCode(memberInfo.getMemCode())
                .name(memberInfo.getName())        // 사용자 이름
                .email(memberInfo.getEmail())      // 사용자 이메일
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresAt(System.currentTimeMillis() + jwtTokenProvider.getAccessTokenValidityInMilliseconds())
                .build();

            log.info("토큰 갱신 성공 - memCode: {}", memberCode);

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("토큰 갱신 실패 - 오류: {}", e.getMessage());
            return ResponseEntity.status(401).body(
                Map.of("error", "Token refresh failed: " + e.getMessage(), "status", 401)
            );
        }
    }
    
    /**
     * 로그아웃 API (보안 우선 httpOnly 쿠키 방식)
     * 세션 및 httpOnly 쿠키(refresh token) 완전 삭제
     */
    @PostMapping("/auth/select/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
    
        // Security 컨텍스트 삭제
        SecurityContextHolder.clearContext();
        
        // httpOnly refresh token 쿠키 삭제 (보안 우선 JWT 방식용)
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // 개발환경: HTTP 허용, 운영환경에서는 true로 변경 필요 ?
        refreshCookie.setPath("/");
        refreshCookie.setAttribute("SameSite", "Strict"); // 개발환경: Lax, 운영환경: strict ?
        response.addCookie(refreshCookie);
    
        return ResponseEntity.ok().body("로그아웃 성공");
    }

    // Info Controller

    // 회원 정보 전체 조회
    @GetMapping("/info/select/entire")
    public ResponseEntity<?> selectEntire(@AuthenticationPrincipal Long memCode){ // Spring Security가 자동으로 JWT 검증 및 사용자 정보 주입, 인증되지 않은 요청은 SecurityConfig에서 401 자동 처리

        MemberInfoResponseDto memberInfo = infoService.getMemberInfo(memCode);

        return ResponseEntity.ok().body(memberInfo);

    }

    // 회원의 잔여 이용권 조회
    @GetMapping("/info/select/ticket")
    public ResponseEntity<String> selectTicket(@AuthenticationPrincipal Long memCode){
        log.debug("이용권 조회 요청 - memCode: {}", memCode);
        Long memberCode = memCode;
        int balance = infoService.getUsageBalance(memberCode);
        int total = infoService.getUsageTotal(memberCode);
    
        // JSON 문자열로 직접 반환 (임시 해결책)
        String jsonResponse = String.format(
            "{\"total\":%d, \"balance\":%d}", 
            total, balance
        );
        
        System.out.println("response: " + jsonResponse);
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(jsonResponse);
    }

    @PatchMapping("/info/update/name")
    public ResponseEntity<String> updateName(@RequestBody UpdateNameRequestDto updateNameRequestDto, @AuthenticationPrincipal Long memCode){
        infoService.updateName(updateNameRequestDto.getMemName(), memCode);
        return ResponseEntity.ok("이름 수정 완료");
    }

    @PatchMapping("/info/update/type")
    public ResponseEntity<String> updateType(@RequestBody UpdateTypeRequestDto updateTypeRequestDto, @AuthenticationPrincipal Long memCode){
        infoService.updateType(updateTypeRequestDto.getMemType(), memCode);
        return ResponseEntity.ok("소속 수정 완료");
    }

    @PatchMapping("/info/update/password")
    public ResponseEntity<String> updatePassword(
            @RequestBody UpdatePasswordRequestDto updatePasswordRequestDto,
            @AuthenticationPrincipal Long memCode
            ){
        // System.out.println("start");
        infoService.updatePassword(
                updatePasswordRequestDto.getCurrentPassword(),
                updatePasswordRequestDto.getNewPassword(),
                updatePasswordRequestDto.getConfirmPassword(),
                memCode
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
