package com.cj.genieq.passage.dto.request;


import com.cj.genieq.passage.dto.DescriptionDto;
import com.cj.genieq.question.dto.request.QuestionInsertRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

//지문 + 저장에 사용
public class PassageWithQuestionsRequestDto {
    private String title;
    private String content;
    private Integer isGenerated;
    private Integer isUserEntered;
    private Long refPasCode; // 부모 지문 참조 코드 추가
    private List<DescriptionDto> descriptions;
    private List<QuestionInsertRequestDto> questions;

    private String mode;
}
