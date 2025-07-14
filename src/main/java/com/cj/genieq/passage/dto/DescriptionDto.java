package com.cj.genieq.passage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescriptionDto {
    private String pasType;    // 인문, 과학, 기술, 예술, 사회, 독서론 (enum 고려)
    private String keyword;    // 키워드 (사용자가 직접 입력한 키워드)
    private String gist;       // 핵심 논점 (ai 가 생성해준 generated_core_point)
    private Integer order;     // 순서 (1, 2), 기본값 1
}
