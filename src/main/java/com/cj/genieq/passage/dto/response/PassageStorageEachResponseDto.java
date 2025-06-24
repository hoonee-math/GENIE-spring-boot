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
public class PassageStorageEachResponseDto {
    private Long pasCode;
    private String pasType;
    private String title;
    private String keyword;
    private Integer isGenerated;
    private LocalDate date;
    private Integer isFavorite;
}
