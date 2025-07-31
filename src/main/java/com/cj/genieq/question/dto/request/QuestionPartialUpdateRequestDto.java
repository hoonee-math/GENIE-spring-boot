package com.cj.genieq.question.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionPartialUpdateRequestDto {
    private String queQuery;      // 문제문
    private String queOption;     // 선택지
    private String queAnswer;     // 정답
    private String queSubpassage; // 보기 내용
    private String queDescription; // 해설
}
