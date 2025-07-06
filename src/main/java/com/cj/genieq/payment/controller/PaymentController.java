package com.cj.genieq.payment.controller;

import com.cj.genieq.member.dto.AuthenticatedMemberDto;
import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.payment.dto.request.PaymentRequestDto;
import com.cj.genieq.payment.dto.response.PaymentListResponseDto;
import com.cj.genieq.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/paym")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // 결제 내역 추가
    @PostMapping("/insert/each")
    public ResponseEntity<?> insertEach(@AuthenticationPrincipal AuthenticatedMemberDto member, @RequestBody PaymentRequestDto paymentRequestDto) {

        //paymentService.insertPayment(member.getMemCode(), paymentRequestDto.getTicCode());

        return ResponseEntity.ok().body("결제 성공");
    }

    // 결제 전체 조회
    @GetMapping("/select/list")
    public ResponseEntity<?> selectList(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal AuthenticatedMemberDto member) {

        List<PaymentListResponseDto> payments = paymentService.getPaymentList(member.getMemCode(), startDate, endDate);
        return ResponseEntity.ok(payments);
    }
}
