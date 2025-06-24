package com.cj.genieq.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PaymentRequestDto {
    private Long ticCode;

    private String orderId;
    private String paymentKey;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private Integer totalAmount;
}

