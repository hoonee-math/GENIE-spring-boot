package com.cj.genieq.tosspay.repository;

import com.cj.genieq.tosspay.entity.TossPayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TossPayRepository extends JpaRepository<TossPayEntity, Long> {

    Optional<TossPayEntity> findByOrderId(String orderId);
}
