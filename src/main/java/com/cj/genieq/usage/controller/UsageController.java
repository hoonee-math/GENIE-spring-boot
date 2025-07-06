package com.cj.genieq.usage.controller;

import com.cj.genieq.member.dto.AuthenticatedMemberDto;
import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.usage.dto.response.UsageListResponseDto;
import com.cj.genieq.usage.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/usag")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/select/list")
    public ResponseEntity<?> selectList(
            @RequestParam("startDate")LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @AuthenticationPrincipal AuthenticatedMemberDto member){

        List<UsageListResponseDto> usages = usageService.getUsageList(member.getMemCode(), startDate, endDate);
        return ResponseEntity.ok(usages);
    }
    
    // 이용권 추가 및 차감에 따른 이용 내역 저장
    // 서비스만 이용할 예정(해당 컨트롤러 사용 안함)
    @PostMapping("/insert/each")
    public ResponseEntity<?> insertEach(){
        // 이용권 추가 시 사용
        //usageService.updateUsage(1L, 1, "이용권 추가");

        // 이용권 차감 시 사용
        usageService.updateUsage(1L, -1, "지문 생성");
        return ResponseEntity.ok("차감 성공");
    }
}
