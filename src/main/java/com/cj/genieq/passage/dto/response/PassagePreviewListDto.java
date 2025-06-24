package com.cj.genieq.passage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PassagePreviewListDto {
    private Long passageCode;
    private String passageTitle;
    private String subjectKeyword;
    private LocalDate date;
    private String content;
    private String gist;
    private Integer favorite;
}
