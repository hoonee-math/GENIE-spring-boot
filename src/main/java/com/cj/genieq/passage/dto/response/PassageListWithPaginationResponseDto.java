package com.cj.genieq.passage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 🔥 통합 Storage API 응답 DTO (페이지네이션 포함)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassageListWithPaginationResponseDto {
    private List<PassageStorageEachResponseDto> items;  // 페이지 데이터
    private int totalCount;        // 전체 아이템 수
    private int totalPages;        // 전체 페이지 수
    private int currentPage;       // 현재 페이지 (1부터 시작)
    private int pageSize;          // 페이지 크기
    private boolean hasMore;       // 다음 페이지 존재 여부
}