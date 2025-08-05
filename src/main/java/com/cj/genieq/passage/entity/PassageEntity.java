package com.cj.genieq.passage.entity;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.question.entity.QuestionEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@Table(name = "passage")
public class PassageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ MariaDB는 IDENTITY
    @Column(name = "pas_code") // ✅ 소문자
    private Long pasCode;

    @Column(name = "pas_title", columnDefinition = "LONGTEXT")
    private String title;

    @Lob
    @Column(name = "pas_content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "pas_date")
    private LocalDateTime date;

    @Column(name = "pas_is_favorite")
    private Integer isFavorite;

    @Column(name = "pas_is_deleted")
    private Integer isDeleted;

    @Column(name = "pas_is_generated")
    private Integer isGenerated;

    @Column(name = "pas_is_user_entered")
    private Integer isUserEntered;

    @Column(name = "ref_pas_code")
    private Long refPasCode;  // 부모 지문 참조 (NULL 가능)

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "mem_code") // ✅ FK도 소문자
    private MemberEntity member;

    @JsonIgnore
    @OneToMany(mappedBy = "passage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionEntity> questions;

    @JsonIgnore
    @OneToMany(mappedBy = "passage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DescriptionEntity> descriptions;
}
