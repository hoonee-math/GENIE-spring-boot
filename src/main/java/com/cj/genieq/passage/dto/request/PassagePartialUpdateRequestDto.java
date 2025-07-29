package com.cj.genieq.passage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassagePartialUpdateRequestDto {

    private String title;           // 제목 (null이면 수정 안함)
    private String content;         // 내용 (null이면 수정 안함)
    private Integer isFavorite;     // 즐겨찾기 (null이면 수정 안함)
}
