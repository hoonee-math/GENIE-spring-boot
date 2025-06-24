package com.cj.genieq.member.service;

import com.cj.genieq.member.dto.request.SignUpRequestDto;
import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.member.repository.MemberRepository;
import com.cj.genieq.usage.service.UsageService;
import jakarta.servlet.http.HttpSession;
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
    // HttpSession은 세션을 관리하기 위한 인터페이스. 사용자가 웹 서버에 접속할 때부터 연결이 종료될 때까지 유지되는 상태 정보를 저장하는데 사용
    public LoginMemberResponseDto login(String memEmail, String memPassword, HttpSession session) {
        //이메일로 사용자 조회
        MemberEntity member = memberRepository.findByMemEmail(memEmail)
                .orElseThrow(()-> new IllegalArgumentException("이메일이 존재하지 않습니다."));
        //회원탈퇴 검증
        if(member.getMemIsDeleted()==1 ) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        //비밀번호 검증
        if(!passwordEncoder.matches(memPassword, member.getMemPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        LoginMemberResponseDto loginMember = LoginMemberResponseDto.builder()
                                                .memberCode(member.getMemCode())
                                                .name(member.getMemName())
                                                .email(member.getMemEmail())
                                                .build();

        //세션에 사용자 정보 저장
        session.setAttribute("LOGIN_USER", loginMember);
        return loginMember;
    }

    @Override
    @Transactional
    public void withdraw(String memEmail,  HttpSession session){
        //이메일로 사용자 조회
        // System.out.println("start");
        MemberEntity member = memberRepository.findByMemEmail(memEmail)
                .orElseThrow(()-> new IllegalArgumentException("회원이 존재하지 않습니다."));
        // System.out.println("mid");
        member.setMemIsDeleted(1);
        memberRepository.save(member);
        // System.out.println("end");
        session.invalidate(); //세션 만료 처리
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
