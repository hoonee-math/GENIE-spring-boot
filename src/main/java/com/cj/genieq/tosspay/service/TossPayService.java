package com.cj.genieq.tosspay.service;

import com.cj.genieq.tosspay.dto.request.TossWebhookPayload;

import java.time.LocalDateTime;

public interface TossPayService {
    void saveTossPay(Long paymentId,
                     String orderId,
                     String paymentKey,
                     String paymentMethod,
                     String paymentStatus,
                     LocalDateTime requestedAt,
                     LocalDateTime approvedAt,
                     Integer totalAmount);

    void process(TossWebhookPayload payload);
}
