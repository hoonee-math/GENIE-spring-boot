package com.cj.genieq.passage.service;

import com.cj.genieq.member.repository.MemberRepository;
import com.cj.genieq.passage.dto.response.ChildPassageDto;
import com.cj.genieq.passage.dto.response.PassageListWithPaginationResponseDto;
import com.cj.genieq.passage.dto.response.PassageStorageEachResponseDto;
import com.cj.genieq.passage.dto.response.SimpleDescriptionDto;
import com.cj.genieq.passage.repository.DescriptionRepository;
import com.cj.genieq.passage.repository.PassageRepository;
import com.cj.genieq.question.repository.QuestionRepository;
import com.cj.genieq.question.service.QuestionService;
import com.cj.genieq.usage.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final PassageRepository passageRepository;

    /**
     * 🔥 통합 Storage 리스트 조회 (최적화됨)
     * 1. Repository에서 DTO 직접 반환 (필수 필드만)
     * 2. descriptions와 childPassages 배치 조회
     * 3. 메모리에서 조합
     */
    @Override
    public Page<PassageStorageEachResponseDto> getPassagesWithFilters(
            Long memCode,
            String listType,
            Pageable pageable,
            String field,
            String search) {

        log.info("🔄 통합 Storage 조회 - memCode: {}, listType: {}, field: {}, search: {}",
                memCode, listType, field, search);

        // 1. 🔥 메인 데이터 조회 (DTO 직접 반환)
        Page<PassageStorageEachResponseDto> dtoPage = passageRepository
                .findPassagesWithFilters(memCode, listType, field, search, pageable);

        if (dtoPage.isEmpty()) {
            log.info("📭 조회 결과 없음 - listType: {}", listType);
            return dtoPage;
        }

        // 2. 🔥 추가 데이터 배치 조회 및 조합
        enrichWithAdditionalData(dtoPage.getContent());

        log.info("✅ 통합 조회 완료 - listType: {}, 아이템 수: {}", listType, dtoPage.getContent().size());
        return dtoPage;
    }

    /**
     * 🔥 통합 Storage API 응답 생성
     */
    @Override
    public PassageListWithPaginationResponseDto getStorageListWithPagination(
            Long memCode,
            String listType,
            int page,
            int size,
            String field,
            String search,
            String sort,
            String order) {

        // 페이지 번호 조정 (프론트엔드는 1부터, JPA는 0부터)
        int pageIndex = Math.max(0, page - 1);

        // 정렬 방향 설정
        Sort.Direction direction = "asc".equalsIgnoreCase(order)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // 정렬 기준 매핑
        String sortField = mapSortField(sort);
        Sort sortBy = Sort.by(direction, sortField);

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(pageIndex, size, sortBy);

        // 통합 메서드 호출
        Page<PassageStorageEachResponseDto> result = getPassagesWithFilters(
                memCode, listType, pageable, field, search
        );

        // 응답 DTO 생성
        return PassageListWithPaginationResponseDto.builder()
                .items(result.getContent())
                .totalCount((int) result.getTotalElements())
                .totalPages(result.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .hasMore(result.hasNext())
                .build();
    }

    /**
     * DTO 리스트에 descriptions와 childPassages 추가
     * - N+1 문제 없이 배치 조회
     * - 메모리에서 효율적으로 조합
     */
    private void enrichWithAdditionalData(List<PassageStorageEachResponseDto> dtoList) {
        if (dtoList.isEmpty()) return;

        List<Long> pasCodeList = dtoList.stream()
                .map(PassageStorageEachResponseDto::getPasCode)
                .collect(Collectors.toList());

        // 🔥 descriptions 배치 조회
        List<SimpleDescriptionDto> allDescriptions =
                passageRepository.findSimpleDescriptionsByPassageCodes(pasCodeList);

        // 🔥 childPassages 배치 조회
        List<ChildPassageDto> allChildPassages =
                passageRepository.findChildPassagesByParentCodes(pasCodeList);

        // 🔥 메모리에서 그룹핑 및 조합
        Map<Long, List<PassageStorageEachResponseDto.SimpleDescriptionInfo>> descriptionMap =
                allDescriptions.stream()
                        .collect(Collectors.groupingBy(
                                SimpleDescriptionDto::pasCode,
                                Collectors.mapping(this::convertToDescriptionInfo, Collectors.toList())
                        ));

        Map<Long, List<PassageStorageEachResponseDto.ChildPassageInfo>> childPassageMap =
                allChildPassages.stream()
                        .collect(Collectors.groupingBy(
                                ChildPassageDto::refPasCode,
                                Collectors.mapping(this::convertToChildPassageInfo, Collectors.toList())
                        ));

        // 🔥 DTO에 추가 데이터 설정
        dtoList.forEach(dto -> {
            dto.setDescriptions(descriptionMap.getOrDefault(dto.getPasCode(), List.of()));
            dto.setChildPassages(childPassageMap.getOrDefault(dto.getPasCode(), List.of()));
        });

        log.debug("✅ 배치 조회 완료 - descriptions: {}, childPassages: {}",
                allDescriptions.size(), allChildPassages.size());
    }

    /**
     * Repository Record → Response DTO 변환 (Description)
     */
    private PassageStorageEachResponseDto.SimpleDescriptionInfo convertToDescriptionInfo(SimpleDescriptionDto record) {
        return PassageStorageEachResponseDto.SimpleDescriptionInfo.builder()
                .pasType(record.pasType())
                .keyword(record.keyword())
                .order(record.order())
                .build();
    }

    /**
     * Repository Record → Response DTO 변환 (ChildPassage)
     */
    private PassageStorageEachResponseDto.ChildPassageInfo convertToChildPassageInfo(ChildPassageDto record) {
        return PassageStorageEachResponseDto.ChildPassageInfo.builder()
                .pasCode(record.pasCode())
                .title(record.title())
                .isGenerated(record.isGenerated())
                .isFavorite(record.isFavorite())
                .date(record.date())
                .refPasCode(record.refPasCode())
                .questionCount(record.questionCount())
                .build();
    }

    /**
     * 정렬 필드 매핑 (프론트엔드 → 백엔드)
     */
    private String mapSortField(String sort) {
        return switch (sort.toLowerCase()) {
            case "date" -> "date";           // 날짜순
            case "title" -> "title";         // 제목순
            case "favorite" -> "isFavorite"; // 즐겨찾기순
            default -> "date";               // 기본값: 날짜순
        };
    }

}
