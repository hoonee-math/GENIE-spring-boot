package com.cj.genieq.member.service;

import com.cj.genieq.common.jwt.JwtTokenProvider;
import com.cj.genieq.member.dto.request.SignUpRequestDto;
import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.member.repository.MemberRepository;
import com.cj.genieq.usage.service.UsageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsageService usageService;
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 제공자 주입

    // 이메일 중복 검사 서비스
    // 이메일 하나만을 전달받아 중복 체크를 수행
    @Override
    public boolean checkEmailDuplicate(String email) {
        // 1이면 중복, 0이면 사용 가능
        return memberRepository.existsByMemEmail(email)==1;
    }

    //  회원가입 처리
    @Override
    @Transactional
    // signUp(SignUpRequestDto signUpRequestDto)는 회원가입에 필요한 데이터를 매개변수로 받음
    public void signUp(SignUpRequestDto signUpRequestDto) {
        if (checkEmailDuplicate(signUpRequestDto.getMemEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getMemPassword());

        // 엔티티에 저장할 값 세팅
        MemberEntity member = MemberEntity.builder()
                .memName(signUpRequestDto.getMemName())
                .memEmail(signUpRequestDto.getMemEmail())
                .memPassword(encodedPassword)
                .memGender(signUpRequestDto.getMemGender())
                .memType(signUpRequestDto.getMemType())
                .build();

        // db에 저장 처리
        MemberEntity savedMember = memberRepository.save(member);

        // 이용권 추가
        usageService.updateUsage(savedMember.getMemCode(), 5, "회원 가입");
    }

    @Override
    /**
     * JWT 기반 로그인 처리
     * 기존 세션 기반에서 JWT 토큰 발급으로 전환
     * @param memEmail 사용자 이메일
     * @param memPassword 사용자 비밀번호
     * @return JWT 토큰이 포함된 로그인 응답 DTO
     */
    public LoginMemberResponseDto login(String memEmail, String memPassword) {
        // 이메일로 사용자 조회
        MemberEntity member = memberRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));
        
        // 회원탈퇴 및 계정 비활성화 검증
        if (member.getMemIsDeleted() == 1) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }
        
        if (!member.isAccountEnabled()) {
            throw new IllegalArgumentException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }
    
        // 비밀번호 검증
        if (!passwordEncoder.matches(memPassword, member.getMemPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
            member.getMemCode(), 
            member.getMemEmail(), 
            member.getRole() != null ? member.getRole() : "ROLE_USER"
        );
        
        // 토큰 만료 시간 계산
        long expiresAt = System.currentTimeMillis() + jwtTokenProvider.getAccessTokenValidityInMilliseconds();
    
        // 로그인 응답 DTO 생성 (기존 필드 + JWT 토큰 필드)
        return LoginMemberResponseDto.builder()
                // 기존 GENIE 필드 (프론트엔드 호환성)
                .memberCode(member.getMemCode())
                .name(member.getMemName())
                .email(member.getMemEmail())
                // JWT 토큰 필드 (새로 추가)
                .accessToken(accessToken)
                //.refreshToken(null) // refresh token은 httpOnly 쿠키로 관리하므로 응답에 포함하지 않음 // 보안 강화: JavaScript로 접근 불가
                .tokenType("Bearer") // 기본값으로 설정됨
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    @Transactional
    /**
     * 회원탈퇴 처리 (JWT 기반으로 전환)
     * 세션 의존성 제거로 JWT 토큰 기반 인증에서는 서버측 세션 처리 불필요
     * @param memEmail 탈퇴할 회원 이메일
     */
    public void withdraw(String memEmail) {
        // 이메일로 사용자 조회
        MemberEntity member = memberRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        
        // 회원 삭제 플래그 설정 (소프트 삭제)
        member.setMemIsDeleted(1);
        memberRepository.save(member);
        
        // JWT 기반에서는 세션 처리 불필요
        // 클라이언트에서 토큰 삭제 처리로 로그아웃 진행
    }

    @Override
    @Transactional
    public void findPassword(String memEmail, String tempPassword){
        //이메일로 사용자 조회
        MemberEntity member = memberRepository.findByMemEmail(memEmail)
                .orElseThrow(()->new IllegalArgumentException("이메일이 존재하지 않습니다."));

        //임시 비밀번호로 업데이트
        String encodePassword = passwordEncoder.encode(tempPassword);
        member.setMemPassword(encodePassword);

        //db에 비밀번호 저장
        memberRepository.save(member);
    }
    
    }
    