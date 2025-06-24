package com.cj.genieq.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class QuestionDto {
    private Long queCode;
    private String queQuery;
    private String queOption;
    private String queAnswer;

    private Long pasCode;
    private String pasTitle;
    private String pasContent;
    private String pasGist;
    private String pasDate;
    private Integer pasIsFavorite;
    private Integer pasIsDeleted;
    private Integer pasIsGenerated;
    private Long memCode;

    private Long subCode;
    private String subType;
    private String subKeyword;
}
