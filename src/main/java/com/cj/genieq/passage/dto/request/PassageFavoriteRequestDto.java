package com.cj.genieq.passage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

//즐겨찾기 추가/해제 요청
// 클라이언트가 서볼 pasCode전송하여 상태 변경 요청
public class PassageFavoriteRequestDto {
    private Long pasCode;
}
