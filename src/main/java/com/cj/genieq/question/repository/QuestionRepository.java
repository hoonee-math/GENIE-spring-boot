package com.cj.genieq.question.repository;

import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.question.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    //기존 문항 삭제 → PassageEntity 기준으로 삭제
    void deleteByPassage(PassageEntity passage);

    //  특정 지문에 속한 문항 가져오기 (필요 시)
    List<QuestionEntity> findByPassage(PassageEntity passage);

}
