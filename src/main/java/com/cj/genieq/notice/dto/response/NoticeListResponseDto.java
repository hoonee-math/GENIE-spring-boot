package com.cj.genieq.notice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class NoticeListResponseDto {
    private Long notCode;
    private String type;
    private String title;
    private LocalDate date;
}
