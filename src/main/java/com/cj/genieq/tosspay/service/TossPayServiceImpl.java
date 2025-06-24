package com.cj.genieq.tosspay.service;

import com.cj.genieq.payment.entity.PaymentEntity;
import com.cj.genieq.tosspay.dto.request.TossWebhookPayload;
import com.cj.genieq.tosspay.entity.TossPayEntity;
import com.cj.genieq.payment.repository.PaymentRepository;
import com.cj.genieq.tosspay.repository.TossPayRepository;
import com.cj.genieq.usage.service.UsageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TossPayServiceImpl implements TossPayService {

    private final TossPayRepository tossPayRepository;
    private final PaymentRepository paymentRepository;
    private final UsageService usageService;

    @Override
    @Transactional
    public void saveTossPay(Long paymentId,
                            String orderId,
                            String paymentKey,
                            String paymentMethod,
                            String paymentStatus,
                            LocalDateTime requestedAt,
                            LocalDateTime approvedAt,
                            Integer totalAmount) {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("결제 정보가 없습니다. id=" + paymentId));

        TossPayEntity tossPay = TossPayEntity.builder()
                .payment(payment)
                .orderId(orderId)
                .paymentKey(paymentKey)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .totalAmount(totalAmount)
                .build();

        tossPayRepository.save(tossPay);
    }

    @Override
    @Transactional
    public void process(TossWebhookPayload payload) {

        if (!"PAYMENT_STATUS_CHANGED".equals(payload.getEventType())) return;

        String orderId = payload.getData().getOrderId();
        String status = payload.getData().getStatus();

        // 2) DB 조회
        TossPayEntity tossPay = tossPayRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Unknown orderId= " + orderId));

        PaymentEntity payment = tossPay.getPayment();

        // 3) 상태별 분기
        switch (status) {

            case "CANCELED":
                if (!"CANCELED".equals(payment.getStatus())) {
                    // 결제 테이블 상태 갱신
                    payment.setStatus("CANCELED");
                    paymentRepository.save(payment);

                    // tosspay 테이블 상태 갱신
                    tossPay.setPaymentStatus(status);
                    tossPayRepository.save(tossPay);
                    
                    // 이용권 차감 (결제한 티켓의 수량만큼 차감)
                    int cancelAmount = payment.getTicket().getTicNumber();
                    usageService.updateUsage(
                            payment.getMember().getMemCode(), -cancelAmount, "결제 취소"
                    );
                }
                break;

            case "DONE":
                break;

            default:
                break;
        }
    }

}
