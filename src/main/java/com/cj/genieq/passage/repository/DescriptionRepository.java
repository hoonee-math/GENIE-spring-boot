package com.cj.genieq.passage.repository;

import com.cj.genieq.passage.entity.DescriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DescriptionRepository extends JpaRepository<DescriptionEntity, Long> {
    
    // pasCode로 Description 리스트를 순서대로 조회
    List<DescriptionEntity> findByPassage_PasCodeOrderByOrderAsc(Long pasCode);
}
