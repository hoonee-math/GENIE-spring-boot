package com.cj.genieq.question.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionUpdateRequestDto {
    private Long queCode;
    private String queQuery; //질문
    private String queOption; //보기
    private String queAnswer; //해설
    private String description; //해설
}
