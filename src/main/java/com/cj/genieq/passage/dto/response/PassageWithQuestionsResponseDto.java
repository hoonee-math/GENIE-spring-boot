package com.cj.genieq.passage.dto.response;

import com.cj.genieq.question.dto.request.QuestionInsertRequestDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PassageWithQuestionsResponseDto {
    private Long pasCode;
    private String title;
    private String type;
    private String keyword;
    private String content;
    private String gist;

    private List<QuestionSelectResponseDto> questions;
}
