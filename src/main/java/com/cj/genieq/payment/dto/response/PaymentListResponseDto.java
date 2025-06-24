package com.cj.genieq.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PaymentListResponseDto {
    private Long payCode;
    private String payName;
    private String price;
    private LocalDate date;
    private String status;
}
