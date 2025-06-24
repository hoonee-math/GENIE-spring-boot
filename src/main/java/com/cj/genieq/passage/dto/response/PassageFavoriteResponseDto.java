package com.cj.genieq.passage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

//즐겨찾기 상태 반환
// 변경된 결과(pasCode, isFavorite)를 프론트에 반환
public class PassageFavoriteResponseDto {
    private Long pasCode;
    private Integer isFavorite;
}
