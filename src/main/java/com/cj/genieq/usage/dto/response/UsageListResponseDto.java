package com.cj.genieq.usage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class UsageListResponseDto {
    private Long usaCode;
    private String usaType;
    private int usaCount;
    private int usaBalance;
    private LocalDate usaDate;
}
