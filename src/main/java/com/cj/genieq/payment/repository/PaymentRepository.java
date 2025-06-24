package com.cj.genieq.payment.repository;

import com.cj.genieq.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Query("""
    SELECT p FROM PaymentEntity p
    WHERE p.member.memCode = :memCode
    AND p.date >= :startDate AND p.date < :endDate
    ORDER BY p.date DESC
    """)
    List<PaymentEntity> findByMemCodeAndDateRange(
            @Param("memCode") Long memCode,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
