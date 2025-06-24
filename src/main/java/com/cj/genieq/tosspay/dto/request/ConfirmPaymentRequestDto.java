package com.cj.genieq.tosspay.dto.request;

import lombok.*;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ConfirmPaymentRequestDto {
    private String orderId;
    private String amount;
    private String paymentKey;
}
