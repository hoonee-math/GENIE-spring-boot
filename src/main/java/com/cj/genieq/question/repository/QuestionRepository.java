package com.cj.genieq.question.repository;

import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.question.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    //기존 문항 삭제 → PassageEntity 기준으로 삭제
    void deleteByPassage(PassageEntity passage);

    //  특정 지문에 속한 문항 가져오기 (필요 시)
    List<QuestionEntity> findByPassage(PassageEntity passage);

    // 기존 메서드들 아래에 추가
    Optional<QuestionEntity> findByQueCodeAndPassage_PasCodeAndPassage_Member_MemCode(Long queCode, Long pasCode, Long memCode);

    // (위 함수를 개선해서 사용) 사용하기 전에 existsByPasCodeAndMember_MemCode 를 이용해서 passage에 대한 권한 우선 확인 후 요청할 것
    Optional<QuestionEntity> findByQueCodeAndPassage_PasCode(Long queCode, Long pasCode);

}
