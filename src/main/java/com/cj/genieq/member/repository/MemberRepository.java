package com.cj.genieq.member.repository;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.member.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

//JpaRepository 상속은 기본적인 crud 메서드 자동 생성
//JpaRepository<MemberEntity, Integer>에서 MemberEntity는 대상 엔티티 클래스, Long은 기본 키 타입
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

  // ========== 기존 GENIE 메서드 ==========
  
  // 이메일 중복 체크
  @Query(value = "SELECT CASE WHEN EXISTS (SELECT 1 FROM member WHERE mem_email = :memEmail) THEN 1 ELSE 0 END", nativeQuery = true)
  int existsByMemEmail(@Param("memEmail") String memEmail);

  // 이메일로 사용자 조회
  // findByMemEmail는 jpa의 규칙에 따라 자동 생성
  // Optional<MemberEntity>은 반환 값이 있을 수도 있고 없을 수도 있으므로 Optional 사용
  Optional<MemberEntity> findByMemEmail(String memEmail);
  
  // ========== OAuth2 소셜 로그인 관련 메서드 ==========
  
  /**
   * 소셜 로그인 제공업체와 제공업체 ID로 사용자 조회
   * OAuth2 소셜 로그인 사용자를 찾기 위한 핵심 메서드
   * @param provider 소셜 로그인 제공업체 (GOOGLE, GITHUB 등)
   * @param providerId 소셜 제공업체에서 발급한 고유 ID
   * @return 해당하는 사용자 (없으면 empty)
   */
  Optional<MemberEntity> findByProviderAndProviderId(SocialProvider provider, String providerId);
  
  /**
   * 이메일로 사용자 조회 (OAuth2 표준 메서드명)
   * 기존 findByMemEmail과 동일하지만 OAuth2 표준에 맞춰 메서드명 또한 제공
   * @param email 이메일 주소
   * @return 해당하는 사용자 (없으면 empty)
   */
  default Optional<MemberEntity> findByEmail(String email) {
      return findByMemEmail(email);
  }
  
  /**
   * 이메일과 소셜 로그인 제공업체로 사용자 조회
   * 동일한 이메일이지만 다른 제공업체로 가입된 경우를 구분하기 위함
   * 예: user@example.com이 LOCAL과 GOOGLE 두 가지로 가입된 경우
   * @param email 이메일 주소
   * @param provider 소셜 로그인 제공업체
   * @return 해당하는 사용자 (없으면 empty)
   */
  Optional<MemberEntity> findByMemEmailAndProvider(String email, SocialProvider provider);
  
  /**
   * 활성화된 계정인지 확인하며 이메일로 사용자 조회
   * 비활성화된 계정은 로그인을 차단하기 위해 사용
   * @param email 이메일 주소
   * @return 활성화된 사용자 (없으면 empty)
   */
  Optional<MemberEntity> findByMemEmailAndEnabledTrue(String email);
  
  /**
   * 비활성화된 계정인지 확인하며 이메일로 사용자 조회
   * 계정 비활성화 처리나 복구를 위해 사용
   * @param email 이메일 주소
   * @return 비활성화된 사용자 (없으면 empty)
   */
  Optional<MemberEntity> findByMemEmailAndEnabledFalse(String email);
  
  /**
   * 소셜 로그인 제공업체별 사용자 존재 여부 확인
   * 중복 가입 방지를 위해 사용
   * @param provider 소셜 로그인 제공업체
   * @param providerId 소셜 제공업체에서 발급한 고유 ID
   * @return 존재하면 true, 없으면 false
   */
  boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
  
  /**
   * 활성화되고 삭제되지 않은 계정인지 확인하며 이메일로 사용자 조회
   * 실제 로그인 가능한 사용자만 조회하기 위해 사용
   * @param email 이메일 주소
   * @return 로그인 가능한 사용자 (없으면 empty)
   */
  @Query("SELECT m FROM MemberEntity m WHERE m.memEmail = :email AND m.enabled = true AND m.memIsDeleted = 0")
  Optional<MemberEntity> findActiveUserByEmail(@Param("email") String email);
  
  /**
   * 활성화되고 삭제되지 않은 소셜 로그인 사용자 조회
   * 소셜 로그인시 실제 로그인 가능한 사용자만 조회하기 위해 사용
   * @param provider 소셜 로그인 제공업체
   * @param providerId 소셜 제공업체에서 발급한 고유 ID
   * @return 로그인 가능한 사용자 (없으면 empty)
   */
  @Query("SELECT m FROM MemberEntity m WHERE m.provider = :provider AND m.providerId = :providerId AND m.enabled = true AND m.memIsDeleted = 0")
  Optional<MemberEntity> findActiveSocialUser(@Param("provider") SocialProvider provider, @Param("providerId") String providerId);
}
