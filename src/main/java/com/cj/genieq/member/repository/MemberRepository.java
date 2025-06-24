package com.cj.genieq.member.repository;

import com.cj.genieq.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

//JpaRepository 상속은 기본적인 crud 메서드 자동 생성
//JpaRepository<MemberEntity, Integer>에서 MemberEntity는 대상 엔티티 클래스, Long은 기본 키 타입
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

  // 이메일 중복 체크
  @Query(value = "SELECT CASE WHEN EXISTS (SELECT 1 FROM member WHERE mem_email = :memEmail) THEN 1 ELSE 0 END", nativeQuery = true)
  int existsByMemEmail(@Param("memEmail") String memEmail);

  // 이메일로 사용자 조회
//    @Query(value = "SELECT * FROM MEMBER WHERE MEM_EMAIL = :memEmail", nativeQuery = true)
//    Optional<MemberEntity> findByMemEmail(@Param("memEmail") String memEmail);

    //findByMemEmail는 jpa의 규칙에 따라 자동 생성
    //Optional<MemberEntity>은 반환 값이 있을 수도 있고 없을 수도 있으므로 Optional 사용
    Optional<MemberEntity> findByMemEmail(String memEmail);
}
