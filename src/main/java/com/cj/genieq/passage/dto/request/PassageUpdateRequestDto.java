package com.cj.genieq.passage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PassageUpdateRequestDto {
    private Long pasCode;
    private String title;
    private String content;
}
