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

//문항 저장에 사용
public class QuestionInsertRequestDto {
    private Long queCode;
    private String queQuery; //질문
    private String queOption; //선택지
    private String queAnswer; //정답
    private String queDescription; //해설
    private String queSubpassage; //박스형 보기에 들어가는 글
}
