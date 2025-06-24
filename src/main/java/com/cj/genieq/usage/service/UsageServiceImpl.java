package com.cj.genieq.usage.service;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.member.repository.MemberRepository;
import com.cj.genieq.usage.dto.response.UsageListResponseDto;
import com.cj.genieq.usage.entity.UsageEntity;
import com.cj.genieq.usage.repository.UsageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService{
    private final UsageRepository usageRepository;
    private final MemberRepository memberRepository;

    public List<UsageListResponseDto> getUsageList(
            Long memCode, LocalDate startDate, LocalDate endDate) {

        LocalDateTime startOfDay = startDate.atStartOfDay();
        LocalDateTime endOfDay = endDate.atTime(23, 59, 59, 999999999);

        List<UsageEntity> usageEntities = usageRepository.findByMemCodeAndDateRange(
                memCode, startOfDay, endOfDay);

        return usageEntities.stream()
                .map(usage -> UsageListResponseDto.builder()
                        .usaCode(usage.getUsaCode())
                        .usaType(usage.getUsaType()) // 한글 값 반환
                        .usaCount(usage.getUsaCount())
                        .usaBalance(usage.getUsaBalance())
                        .usaDate(usage.getUsaDate().toLocalDate())
                        .build())
                .collect(Collectors.toList());
    }

    // 이용권 추가/차감 후 내역 저장 (잔액 업데이트)
    // 매개변수 : 회원코드, 이용권 수(추가 시 양수, 차감 시 음수), 내역 종류
    @Transactional
    public void updateUsage(Long memCode, int count, String type) {
        // 1. 회원 정보 조회
        MemberEntity member = memberRepository.findById(memCode)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 2. 가장 최근 잔액을 가져옴
        Integer lastBalance = 0;
        Optional<Integer> latestBalanceOptional = usageRepository.findLatestBalanceByMemberCode(memCode);

        if (latestBalanceOptional.isPresent()) {
            // 값을 사용
            lastBalance = latestBalanceOptional.get();
        }

        // 3. 새로운 잔액 계산
        int newBalance = lastBalance + count;

        // 4. 새로운 잔액이 음수일 경우, 적절한 예외 처리
        if (newBalance < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        // 5. 이용 내역 저장
        UsageEntity usage = UsageEntity.builder()
                .usaType(type)  // 이용권 추가 내역
                .usaDate(LocalDateTime.now())
                .usaCount(count)  // 추가된 이용권 횟수
                .usaBalance(newBalance)  // 계산된 잔액
                .member(member)
                .build();

        usageRepository.save(usage);  // 이용 내역 저장
    }
}
