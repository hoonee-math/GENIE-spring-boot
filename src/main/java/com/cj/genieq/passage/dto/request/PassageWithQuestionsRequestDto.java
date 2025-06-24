package com.cj.genieq.passage.dto.request;


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
    private String type;
    private String keyword;
    private String title;
    private String content;
    private String gist;
    private Integer isGenerated;
    private List<QuestionInsertRequestDto> questions;

    private String mode;
}
