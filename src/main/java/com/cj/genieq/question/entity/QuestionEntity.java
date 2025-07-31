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
    private String queQuery; // 문제문(문항에서 사용하는 실제 질문) '위 지문의 핵신 논점으로 가장 알맞은 것을 고르기오.', '위 지문과 다음 보기를 읽고 주인공의 심정을 유추한 것으로 가장 알맞은 것을 고르시오.'

    @Lob
    @Column(name = "que_option", columnDefinition = "LONGTEXT")
    private String queOption; // 선택지: '<p>① ~~</p><p>② ~~</p><p>③ ~~</p><p>④ ~~</p><p>⑤ ~~</p>'

    @Lob
    @Column(name = "que_answer", columnDefinition = "LONGTEXT")
    private String queAnswer; // 답: '③'

    @Lob
    @Column(name = "que_subpassage", columnDefinition = "LONGTEXT")
    private String queSubpassage; // 보기 내용(보기는 nullable, 보기가 필요한 문항에만 포함됨)

    @Lob
    @Column(name = "que_description", columnDefinition = "LONGTEXT")
    private String queDescription; // 문항에 대한 정답 해설: '<p>[정답해설]</p><p>이 글은 ~~</p>'

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pas_code") // ✅ FK도 소문자
    private PassageEntity passage;
}
