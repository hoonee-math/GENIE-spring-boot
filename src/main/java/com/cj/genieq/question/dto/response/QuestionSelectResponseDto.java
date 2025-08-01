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
    private String queOption;
    private String queAnswer;
    private String queDescription; //해설
    private String queSubpassage; //박스형 보기에 들어가는 글
}
