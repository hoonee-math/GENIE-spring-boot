package com.cj.genieq.payment.service;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.member.repository.MemberRepository;
import com.cj.genieq.payment.dto.response.PaymentListResponseDto;
import com.cj.genieq.payment.entity.PaymentEntity;
import com.cj.genieq.payment.repository.PaymentRepository;
import com.cj.genieq.ticket.entity.TicketEntity;
import com.cj.genieq.ticket.repository.TicketRepository;
import com.cj.genieq.tosspay.service.TossPayService;
import com.cj.genieq.usage.service.UsageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final MemberRepository memberRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final TossPayService tossPayService;
    private final UsageService usageService;

    // 결제 내역 추가
    @Override
    @Transactional
    public void insertPayment(Long memCode, Long ticCode,String orderId,String paymentKey,String paymentMethod,LocalDateTime requestedAt,LocalDateTime approvedAt,Integer totalAmount) {
        MemberEntity member = memberRepository.findById(memCode)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));

        TicketEntity ticket = ticketRepository.findById(ticCode)
                .orElseThrow(() -> new EntityNotFoundException("티켓이 존재하지 않습니다."));

        PaymentEntity payment = PaymentEntity.builder()
                .price(ticket.getPrice())
                .date(LocalDateTime.now())
                .status("PAID")
                .member(member)
                .ticket(ticket)
                .build();
        try {
            // 결제 저장
            paymentRepository.save(payment);

            // 토스 결제 저장
            tossPayService.saveTossPay(payment.getPayCode(),orderId,paymentKey,paymentMethod,"PAID",requestedAt,approvedAt,totalAmount);

            // 이용권 추가
            usageService.updateUsage(memCode, ticket.getTicNumber(), "이용권 결제");

        } catch (Exception e) {
            throw new RuntimeException("결제 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 결제 전체 조회
    @Override
    public List<PaymentListResponseDto> getPaymentList(Long memCode, LocalDate startDate, LocalDate endDate) {

        LocalDateTime startOfDay = startDate.atStartOfDay();
        LocalDateTime endOfDay = endDate.atTime(23, 59, 59, 999999999);

        List<PaymentEntity> paymentEntities = paymentRepository.findByMemCodeAndDateRange(memCode, startOfDay, endOfDay);

        return paymentEntities.stream()
                .map(payment -> {

                    String payName = "지문/문항 생성 " + payment.getTicket().getTicNumber() + "회 이용권";
                    String price = String.format("%,d 원", payment.getPrice());

                    return PaymentListResponseDto.builder()
                        .payCode(payment.getPayCode())
                        .payName(payName) // 한글 값 반환
                        .price(price)
                        .date(payment.getDate().toLocalDate())
                        .build();
                })
                .collect(Collectors.toList());
    }
}
