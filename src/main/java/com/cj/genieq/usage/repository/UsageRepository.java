package com.cj.genieq.usage.repository;

import com.cj.genieq.usage.entity.UsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface UsageRepository extends JpaRepository<UsageEntity, Long> {

    @Query("""
        SELECT u FROM UsageEntity u
        WHERE u.member.memCode = :memCode
        AND u.usaDate >= :startDate AND u.usaDate < :endDate
        ORDER BY u.usaDate DESC
    """)
    List<UsageEntity> findByMemCodeAndDateRange(
            @Param("memCode") Long memCode,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
    SELECT u.usa_balance
    FROM usage_log u
    WHERE u.mem_code = :memCode
    ORDER BY u.usa_date DESC
    LIMIT 1
""", nativeQuery = true)
    Optional<Integer> findLatestBalanceByMemberCode(@Param("memCode") Long memCode);

    @Query(value = """
        SELECT COALESCE(SUM(u.usa_count), 0)
        FROM usage_log u
        WHERE u.mem_code   = :memCode
          AND u.usa_count >  0
        """,
            nativeQuery = true
    )
    Optional<Integer> findPositiveSumByMemberCode(@Param("memCode") Long memCode);
}
