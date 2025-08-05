package com.cj.genieq.passage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 🔥 최적화된 Storage 아이템 DTO
 * Repository에서 기본 필드만 채우고, Service에서 배치 조회로 추가 데이터 설정
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassageStorageEachResponseDto {

    // ===== Repository에서 직접 채워지는 필드들 =====
    private Long pasCode;          // 지문 코드
    private String title;          // 지문 제목
    private Integer isGenerated;   // 생성 여부 (1: AI생성, 0: 지문+문항)
    private LocalDateTime date;    // 작업 날짜
    private Integer isFavorite;    // 즐겨찾기 여부

    // ===== Service에서 배치 조회로 채워질 필드들 =====
    private List<SimpleDescriptionInfo> descriptions;  // 모든 description 정보
    private List<ChildPassageInfo> childPassages;      // 하위 지문+문항 리스트

    /**
     * 🔥 프론트엔드용 Description 정보 (내부 클래스)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleDescriptionInfo {
        private String pasType;     // 분야 (인문, 사회, 예술, 과학, 기술, 독서론)
        private String keyword;     // 제재 키워드
        private Integer order;      // 순서 (1, 2, 3...)
    }

    /**
     * 🔥 프론트엔드용 하위 지문+문항 정보 (내부 클래스)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildPassageInfo {
        private Long pasCode;       // 지문+문항 코드
        private String title;       // 지문+문항 제목
        private Integer isGenerated; // 항상 0 (지문+문항)
        private LocalDateTime date; // 생성 날짜
        private Long refPasCode;    // 부모 지문 참조
        private Integer questionCount; // 문항 개수
    }

    public PassageStorageEachResponseDto(
            Long pasCode,
            String title,
            Integer isGenerated,
            LocalDateTime date,
            Integer isFavorite
    ) {
        this.pasCode = pasCode;
        this.title = title;
        this.isGenerated = isGenerated;
        this.date = date;
        this.isFavorite = isFavorite;
    }

}