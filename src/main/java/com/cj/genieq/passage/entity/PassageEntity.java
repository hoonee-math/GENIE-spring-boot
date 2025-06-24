package com.cj.genieq.passage.entity;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.question.entity.QuestionEntity;
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

    @Column(name = "pas_type")
    private String pasType;

    @Column(name = "pas_keyword")
    private String keyword;

    @Column(name = "pas_title", columnDefinition = "LONGTEXT")
    private String title;

    @Lob
    @Column(name = "pas_content", columnDefinition = "LONGTEXT")
    private String content;

    @Lob
    @Column(name = "pas_gist", columnDefinition = "LONGTEXT")
    private String gist;

    @Column(name = "pas_date")
    private LocalDateTime date;

    @Column(name = "pas_is_favorite")
    private Integer isFavorite;

    @Column(name = "pas_is_deleted")
    private Integer isDeleted;

    @Column(name = "pas_is_generated")
    private Integer isGenerated;

    @ManyToOne
    @JoinColumn(name = "mem_code") // ✅ FK도 소문자
    private MemberEntity member;

    @OneToMany(mappedBy = "passage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionEntity> questions;
}
