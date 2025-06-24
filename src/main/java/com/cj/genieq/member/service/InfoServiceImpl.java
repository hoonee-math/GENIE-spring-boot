package com.cj.genieq.member.service;

import com.cj.genieq.member.dto.request.UpdateNameRequestDto;
import com.cj.genieq.member.dto.request.UpdatePasswordRequestDto;
import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import com.cj.genieq.member.dto.response.MemberInfoResponseDto;
import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.member.repository.MemberRepository;
import com.cj.genieq.usage.repository.UsageRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InfoServiceImpl implements InfoService {
    private final MemberRepository memberRepository;
    private final UsageRepository usageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberInfoResponseDto getMemberInfo(Long memCode) {
        MemberEntity member = memberRepository.findById(memCode)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 존재하지 않습니다."));

        MemberInfoResponseDto memberInfo = MemberInfoResponseDto.builder()
                .memCode(member.getMemCode())
                .name(member.getMemName())
                .email(member.getMemEmail())
                .gender(member.getMemGender())
                .memType(member.getMemType())
                .build();

        return memberInfo;
    }

    @Override
    public int getUsageBalance(Long memCode) {
        Optional<Integer> latestBalanceOptional = usageRepository.findLatestBalanceByMemberCode(memCode);

        if (latestBalanceOptional.isPresent()) {
            return latestBalanceOptional.get();
        } else{
            throw new EntityNotFoundException("사용자 정보가 없습니다. memCode: " + memCode);
        }
    }

    @Override
    public int getUsageTotal(Long memCode) {
        Optional<Integer> usageTotal = usageRepository.findPositiveSumByMemberCode(memCode);

        if (usageTotal.isPresent()) {
            return usageTotal.get();
        } else{
            throw new EntityNotFoundException("사용자 정보가 없습니다. memCode: " + memCode);
        }
    }

    @Override
    @Transactional
    public void updateName(String memName, HttpSession session) {
        // 세션에서 DTO로 가져오기
        LoginMemberResponseDto loginUser = (LoginMemberResponseDto) session.getAttribute("LOGIN_USER");

        if (loginUser == null) {
            throw new IllegalArgumentException("세션이 만료되었거나 로그인되지 않았습니다.");
        }


        // 이메일 가져오기
        String memEmail = loginUser.getEmail();

        //이메일로 사용자 조회
        MemberEntity member = memberRepository.findByMemEmail(memEmail)
                .orElseThrow(()-> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        //이름 수정 후 저장
        member.setMemName(memName);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateType(String memType, HttpSession session) {
        //세션에서 dto로 가져오기
        LoginMemberResponseDto loginUser = (LoginMemberResponseDto) session.getAttribute("LOGIN_USER");

        if (loginUser == null) {
            throw new IllegalArgumentException("세션이 만료되었거나 로그인되지 않았습니다.");
        }

        //이메일 가져오기
        String memEmail = loginUser.getEmail();

        //이메일로 사용자 조회

        MemberEntity member = memberRepository.findByMemEmail(memEmail)
                .orElseThrow(()->new IllegalArgumentException("사용자가 존재하지 않습니다."));

        //소속 수정 후 저장
        member.setMemType(memType);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updatePassword(String currentPassword, String newPassword, String confirmPassword, HttpSession session){
        //세션에서 사용자 정보 가져오기
        LoginMemberResponseDto loginUser = (LoginMemberResponseDto) session.getAttribute("LOGIN_USER");

        if (loginUser == null) {
            throw new IllegalArgumentException("세션이 만료되었거나 로그인되지 않았습니다.");
        }

        //이메일 가져오기
        String memEmail = loginUser.getEmail();

        //이메일로 사용자 조회
        MemberEntity member = memberRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        //현재 비밀번호 검증
        if(!passwordEncoder.matches(currentPassword, member.getMemPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        //새 비밀번호와 비밀번호 확인이 일치하는지 검증
        if(!newPassword.equals(confirmPassword)){
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        //새로운 비밀번호 암호화 후 저장
        String encryptPassword = passwordEncoder.encode(newPassword);
        member.setMemPassword(encryptPassword);
        memberRepository.save(member);

    }
}
