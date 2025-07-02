package com.cj.genieq.member.entity;

import com.cj.genieq.member.enums.SocialProvider;
import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.payment.entity.PaymentEntity;
import com.cj.genieq.usage.entity.UsageEntity;
import com.fasterxml.jackson.annotation.JsonIgnore; // JSON 직렬화 시 제외용
import jakarta.persistence.*; // JPA 엔티티 어노테이션
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member") // ✅ 테이블 이름 소문자
@Data
@ToString(exclude = {"usages", "passages", "payments"})  // ← 이거 하나만 추가하면 끝!
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ 시퀀스 → IDENTITY
    @Column(name = "mem_code") // ✅ 컬럼명도 소문자
    private Long memCode;

    @Column(name = "mem_name", nullable = false)
    private String memName;

    @Column(name = "mem_email", nullable = false)
    private String memEmail;

    @Column(name = "mem_password", nullable = false)
    private String memPassword;

    @Column(name = "mem_gender")
    private String memGender;

    @Column(name = "mem_type")
    private String memType;

    @Column(name = "mem_is_deleted", nullable = false)
    private int memIsDeleted;

    // ========== OAuth2 소셜 로그인 관련 필드 ==========
    
    /**
     * 소셜 로그인 제공업체 (LOCAL, GOOGLE, GITHUB 등)
     * 기본값: LOCAL (일반 회원가입)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    @Builder.Default
    private SocialProvider provider = SocialProvider.LOCAL;
    
    /**
     * 소셜 서비스의 고유 ID (OAuth2 제공업체에서 발급한 고유 식별자)
     * 일반 회원가입의 경우 null
     */
    @Column(name = "provider_id")
    private String providerId;
    
    /**
     * 프로필 이미지 URL (소셜 로그인시 제공되는 프로필 이미지)
     * 일반 회원가입의 경우 null
     */
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    /**
     * 계정 활성화 여부
     * 기본값: true (활성화)
     */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * 사용자 권한
     * 기본값: ROLE_USER
     */
    @Column(name = "role", nullable = false)
    @Builder.Default
    private String role = "ROLE_USER";
    
    /**
     * 계정 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 계정 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== 연관 관계 매핑 ==========

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<UsageEntity> usages = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PassageEntity> passages = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PaymentEntity> payments = new ArrayList<>();
    
    // ========== 편의 메서드 ==========
    
    /**
     * 일반 회원가입 사용자인지 확인
     * @return provider가 LOCAL이면 true
     */
    public boolean isLocalUser() {
        return provider == SocialProvider.LOCAL;
    }
    
    /**
     * 소셜 로그인 사용자인지 확인
     * @return provider가 LOCAL이 아니면 true
     */
    public boolean isSocialUser() {
        return provider != SocialProvider.LOCAL;
    }
    
    /**
     * 계정이 활성화되어 있는지 확인
     * @return enabled가 true이면 true
     */
    public boolean isAccountEnabled() {
        return enabled != null && enabled;
    }
    
    /**
     * 계정이 삭제되었는지 확인
     * @return memIsDeleted가 1이면 true
     */
    public boolean isDeleted() {
        return memIsDeleted == 1;
    }
}
