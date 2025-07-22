package com.cj.genieq.passage.repository;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.passage.dto.response.PassagePreviewListDto;
import com.cj.genieq.passage.entity.DescriptionEntity;
import com.cj.genieq.passage.entity.PassageEntity;
import com.itextpdf.commons.utils.JsonUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PassageRepository extends JpaRepository<PassageEntity, Long> {

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
}
