package com.cj.genieq.passage.service;

import com.cj.genieq.passage.dto.response.PassageListWithPaginationResponseDto;
import com.cj.genieq.passage.dto.response.PassageStorageEachResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StorageService {

    Page<PassageStorageEachResponseDto> getPassagesWithFilters(
            Long memCode,
            String listType,
            Pageable pageable,
            String field,
            String search
    );

    PassageListWithPaginationResponseDto getStorageListWithPagination(
            Long memCode,
            String listType,
            int page,
            int size,
            String field,
            String search,
            String sort,
            String order
    );

}
