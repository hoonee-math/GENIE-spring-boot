package com.cj.genieq.passage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PassageSelectResponseDto {
    private Long pasCode;
    private String title;
    private String type;
    private String keyword;
    private String content;
    private String gist;
}
