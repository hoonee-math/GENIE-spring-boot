package com.cj.genieq.passage.dto.response;

/**
 * 🔥 간단한 Description DTO (Record 기반)
 * Repository 배치 조회용
 */
public record SimpleDescriptionDto(
        Long pasCode,      // 어떤 지문의 description인지
        String pasType,    // 분야 (인문, 사회, 예술, 과학, 기술, 독서론)
        String keyword,    // 제재 키워드
        Integer order      // 순서 (1, 2, 3...)
) {}