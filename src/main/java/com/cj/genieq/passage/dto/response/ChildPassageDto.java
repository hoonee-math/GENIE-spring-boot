package com.cj.genieq.passage.dto.response;

import java.time.LocalDateTime;

/**
 * 하위 지문+문항 DTO (Record 기반)
 * Repository 배치 조회용
 */
public record ChildPassageDto(
        Long pasCode,              // 지문+문항 코드
        String title,              // 지문+문항 제목
        Integer isGenerated,       // 항상 0 (지문+문항)
        Integer isFavorite,        // 즐겨찾기
        LocalDateTime date,        // 생성 날짜
        Long refPasCode,          // 부모 지문 참조
        Integer questionCount     // 문항 개수
) {}