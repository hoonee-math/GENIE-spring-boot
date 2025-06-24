package com.cj.genieq.question.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

//저장된 문항 반환
public class QuestionSelectResponseDto {
    private Long queCode;
    private String queQuery;
    private List<String> queOption;
    private String queAnswer;
    private String description; //해설
}
