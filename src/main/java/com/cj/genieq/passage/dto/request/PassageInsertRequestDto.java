package com.cj.genieq.passage.dto.request;

import com.cj.genieq.passage.dto.DescriptionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PassageInsertRequestDto {
    private String title;
    private String content;
    private Integer isGenerated;
    private Integer isUserEntered;
    private List<DescriptionDto> descriptions;
}
