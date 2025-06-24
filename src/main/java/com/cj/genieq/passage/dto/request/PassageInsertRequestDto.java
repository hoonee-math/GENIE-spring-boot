package com.cj.genieq.passage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PassageInsertRequestDto {
    private String type;
    private String keyword;
    private String title;
    private String content;
    private String gist;
    private Integer isGenerated;
}
