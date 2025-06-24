package com.cj.genieq.passage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PassageStorageMainResponseDto {
    List<PassageStorageEachResponseDto> favorites;
    List<PassageStorageEachResponseDto> recent;
}
