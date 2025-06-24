package com.cj.genieq.payment.controller;

import com.cj.genieq.member.dto.response.LoginMemberResponseDto;
import com.cj.genieq.payment.dto.request.PaymentRequestDto;
import com.cj.genieq.payment.dto.response.PaymentListResponseDto;
import com.cj.genieq.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> insertEach(HttpSession session, @RequestBody PaymentRequestDto paymentRequestDto) {
        LoginMemberResponseDto loginMember = (LoginMemberResponseDto) session.getAttribute("LOGIN_USER");

        System.out.println("세션 확인용 : 결제"+loginMember);
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        //paymentService.insertPayment(loginMember.getMemberCode(), paymentRequestDto.getTicCode());

        return ResponseEntity.ok().body("결제 성공");
    }

    // 결제 전체 조회
    @GetMapping("/select/list")
    public ResponseEntity<?> selectList(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            HttpSession session) {
        LoginMemberResponseDto loginMember = (LoginMemberResponseDto) session.getAttribute("LOGIN_USER");

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        List<PaymentListResponseDto> payments = paymentService.getPaymentList(loginMember.getMemberCode(), startDate, endDate);
        return ResponseEntity.ok(payments);
    }
}
