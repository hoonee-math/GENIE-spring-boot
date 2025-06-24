package com.cj.genieq.tosspay.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TossPayRequestDto {

    private String orderId;
    private String amount;
    private Long ticCode;
}
