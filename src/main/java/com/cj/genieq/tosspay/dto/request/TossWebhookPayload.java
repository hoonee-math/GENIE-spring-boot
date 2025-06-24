package com.cj.genieq.tosspay.dto.request;

import com.cj.genieq.payment.dto.Payment;
import lombok.*;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TossWebhookPayload {

    private String eventType;
    private String createdAt;
    private Payment data;

    @Getter
    public static class Payment {
        private String orderId;
        private String paymentKey;
        private String status;
        private Integer totalAmount;
    }
}
