package com.cj.genieq.passage.dto.response;

import com.cj.genieq.passage.dto.DescriptionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PassageStorageEachResponseDto {
    private Long pasCode;
    private String title;
    private Integer isGenerated;
    private LocalDate date;
    private Integer isFavorite;
    private List<DescriptionDto> descriptions;
}
