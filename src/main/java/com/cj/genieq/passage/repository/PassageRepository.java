package com.cj.genieq.passage.repository;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.passage.dto.response.ChildPassageDto;
import com.cj.genieq.passage.dto.response.PassagePreviewListDto;
import com.cj.genieq.passage.dto.response.PassageStorageEachResponseDto;
import com.cj.genieq.passage.dto.response.SimpleDescriptionDto;
import com.cj.genieq.passage.entity.DescriptionEntity;
import com.cj.genieq.passage.entity.PassageEntity;
import com.itextpdf.commons.utils.JsonUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassageRepository extends JpaRepository<PassageEntity, Long> {

    // 권한 및 Entity 확인용 메서드
    Optional<PassageEntity> findByPasCodeAndMember_MemCode(Long pasCode, Long memCode);
    // 권한 확인용 메서드
    boolean existsByPasCodeAndMember_MemCode(Long pasCode, Long memCode);

    @Query(value = """
        SELECT * FROM passage p
        WHERE p.mem_code = :memCode
        AND LOWER(p.pas_title) LIKE LOWER(:keyword)
        AND p.pas_is_deleted = 0
        ORDER BY p.pas_date DESC
        LIMIT :count OFFSET :offset
        """, nativeQuery = true)
    List<PassageEntity> findByMemCodeAndKeyword(
            @Param("memCode") Long memCode,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("count") int count
    );

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM PassageEntity p WHERE p.title = :title")
    boolean existsByTitle(@Param("title") String title);

    @Query("SELECT p FROM PassageEntity p WHERE p.member.memCode = :memCode AND p.isGenerated = 1 AND p.isDeleted = 0 ORDER BY p.date DESC")
    List<PassageEntity> findGeneratedPassagesByMember(@Param("memCode") Long memCode);

    @Query(value = """
        SELECT p.* FROM passage p
        WHERE p.mem_code = :memCode
        AND (:isFavorite = 0 OR p.pas_is_favorite = :isFavorite)
        AND p.pas_is_deleted = 0
        ORDER BY p.pas_date DESC
        LIMIT :rn
        """, nativeQuery = true)
    List<PassageEntity> selectPassageListInStorage(
            @Param("memCode") Long memCode,
            @Param("isFavorite") Integer isFavorite,
            @Param("rn") Integer rn
    );

    @Query(value = """
        SELECT p.* FROM passage p
        WHERE p.mem_code = :memCode
        AND p.pas_is_favorite = 1
        AND p.pas_is_deleted = 0
        ORDER BY p.pas_date DESC
        LIMIT 150
        """, nativeQuery = true)
    List<PassageEntity> selectTop150FavoritePassages(@Param("memCode") Long memCode);

    @Query(value = """
        SELECT p.* FROM passage p
        WHERE p.mem_code = :memCode
        AND p.pas_is_deleted = 0
        ORDER BY p.pas_date DESC
        LIMIT 150
        """, nativeQuery = true)
    List<PassageEntity> selectTop150RecentPassages(@Param("memCode") Long memCode);

    @Modifying
    @Query("UPDATE PassageEntity p SET p.isDeleted = 1 WHERE p.pasCode IN :pasCodeList")
    int updateIsDeletedByPasCodeList(@Param("pasCodeList") List<Long> pasCodeList);

    @Modifying
    @Query("UPDATE PassageEntity p SET p.title = :title WHERE p.pasCode = :pasCode")
    int updateTitleByPasCode(@Param("pasCode") Long pasCode, @Param("title") String title);

    @Query("SELECT COUNT(p) FROM PassageEntity p WHERE p.member.memCode = :memCode AND p.isDeleted = :isDeleted")
    int countByMemberAndIsDeleted(@Param("memCode") Long memCode, @Param("isDeleted") Integer isDeleted);

    @Query("SELECT p FROM PassageEntity p " +
            " WHERE p.member.memCode = :memCode " +
            "   AND p.isDeleted = 1")
    List<PassageEntity> findDeletedByMember(@Param("memCode") Long memCode);

    // 1. 기본 정보만 조회 (기존 쿼리 수정)
    @Query("SELECT new com.cj.genieq.passage.dto.response.PassagePreviewListDto(" +
            "p.pasCode, p.title, p.content,null) " +
            "FROM PassageEntity p " +
            "WHERE p.member.memCode = :memCode AND p.isGenerated = 1 AND p.isDeleted = 0 " +
            "AND (:isFavorite IS NULL OR p.isFavorite = :isFavorite) " +
            "ORDER BY p.date DESC " +
            "LIMIT 10")
    List<PassagePreviewListDto> findPassagePreviewsByMember(@Param("memCode") Long memCode,
                                                            @Param("isFavorite") Integer isFavorite);

    // 2. Description 조회용 메소드
    @Query("SELECT d FROM DescriptionEntity d " +
            "WHERE d.passage.pasCode IN :pasCodes " +
            "ORDER BY d.passage.pasCode, d.order")
    List<DescriptionEntity> findDescriptionsByPassageCodes(@Param("pasCodes") List<Long> pasCodes);



    /**
     * 🔥 통합 Storage 리스트 조회 (DTO 직접 반환)
     * @param listType: "recent", "favorite", "deleted"
     * @param field: 분야 필터 (인문, 사회, 예술, 과학, 기술, 독서론)
     * @param search: 검색어 (제목, 키워드 대상)
     */
    @Query("""
    SELECT new com.cj.genieq.passage.dto.response.PassageStorageEachResponseDto(
        p.pasCode,
        p.title, 
        p.isGenerated,
        p.date,
        p.isFavorite
    )
    FROM PassageEntity p 
    LEFT JOIN p.descriptions d ON d.order = 1
    WHERE p.member.memCode = :memCode 
    AND p.refPasCode IS NULL
    AND (
        (:listType = 'recent' AND p.isDeleted = 0) OR
        (:listType = 'favorite' AND p.isDeleted = 0 AND p.isFavorite = 1) OR
        (:listType = 'deleted' AND p.isDeleted = 1)
    )
    AND (p.isGenerated = 1 OR p.isUserEntered = 1)
    AND (:field IS NULL OR :field = '' OR d.pasType = :field)
    AND (:search IS NULL OR :search = '' OR 
         LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
         LOWER(d.keyword) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<PassageStorageEachResponseDto> findPassagesWithFilters(
            @Param("memCode") Long memCode,
            @Param("listType") String listType,
            @Param("field") String field,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * 🔥 특정 지문들의 모든 descriptions 배치 조회
     */
    @Query("""
    SELECT new com.cj.genieq.passage.dto.response.SimpleDescriptionDto(
        d.passage.pasCode,
        d.pasType,
        d.keyword,
        d.order
    )
    FROM DescriptionEntity d 
    WHERE d.passage.pasCode IN :pasCodeList
    ORDER BY d.passage.pasCode, d.order
    """)
    List<SimpleDescriptionDto> findSimpleDescriptionsByPassageCodes(@Param("pasCodeList") List<Long> pasCodeList);

    /**
     * 🔥 특정 지문들의 childPassages 배치 조회
     */
    @Query("""
    SELECT new com.cj.genieq.passage.dto.response.ChildPassageDto(
        p.pasCode,
        p.title,
        p.isGenerated,
        p.date,
        p.refPasCode,
        SIZE(p.questions)
    )
    FROM PassageEntity p 
    WHERE p.refPasCode IN :parentPasCodeList 
    AND p.isDeleted = 0
    ORDER BY p.refPasCode, p.date DESC
    """)
    List<ChildPassageDto> findChildPassagesByParentCodes(@Param("parentPasCodeList") List<Long> parentPasCodeList);
}
