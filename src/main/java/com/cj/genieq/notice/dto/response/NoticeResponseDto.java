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
public class NoticeResponseDto {
    private Long notCode;
    private String title;
    private String content;
    private LocalDate date;
    private String type;
}
