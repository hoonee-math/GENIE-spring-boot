package com.cj.genieq.payment.service;

import com.cj.genieq.payment.dto.response.PaymentListResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    void insertPayment(Long memCode,
                       Long ticCode,
                       String orderId,
                       String paymentKey,
                       String paymentMethod,
                       LocalDateTime requestedAt,
                       LocalDateTime approvedAt,
                       Integer totalAmount);
    List<PaymentListResponseDto> getPaymentList(
            Long memCode, LocalDate startDate, LocalDate endDate);
}
