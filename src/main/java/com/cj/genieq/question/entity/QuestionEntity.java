package com.cj.genieq.question.entity;

import com.cj.genieq.passage.entity.PassageEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@Table(name = "question") // ✅ 소문자 테이블명
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ 시퀀스 제거, IDENTITY로 변경
    @Column(name = "que_code") // ✅ 소문자 컬럼
    private Long queCode;

    @Lob
    @Column(name = "que_query", columnDefinition = "LONGTEXT")
    private String queQuery;

    @Lob
    @Column(name = "que_option", columnDefinition = "LONGTEXT")
    private String queOption;

    @Lob
    @Column(name = "que_answer", columnDefinition = "LONGTEXT")
    private String queAnswer;

    @Lob
    @Column(name = "que_subpassage", columnDefinition = "LONGTEXT")
    private String queSubpassage;

    @Lob
    @Column(name = "que_description", columnDefinition = "LONGTEXT")
    private String queDescription;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pas_code") // ✅ FK도 소문자
    private PassageEntity passage;
}
