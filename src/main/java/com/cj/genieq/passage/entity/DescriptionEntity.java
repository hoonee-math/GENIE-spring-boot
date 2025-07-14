package com.cj.genieq.passage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@Table(name = "description")
public class DescriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ MariaDB는 IDENTITY
    @Column(name = "des_code") // ✅ 소문자
    private Long desCode;

    @Column(name = "pas_type")
    private String pasType;     // 인문, 과학, 기술, 예술, 사회, 독서론 (enum 고려)

    @Column(name = "pas_keyword")
    private String keyword;     // 키워드 (사용자가 직접 입력한 키워드)

    @Lob
    @Column(name = "pas_gist", columnDefinition = "LONGTEXT")
    private String gist;        // generated_core_point

    @Column(name = "pas_order")
    @Builder.Default
    private int order=1;        // 순서 (1, 2), 기본값 1

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pas_code") // ✅ FK도 소문자
    private PassageEntity passage;  // 프론트엔드의 methodType 이 single 이거나 reading 인 경우 1:1 관계, multiple 인 경우 2:1 관계 (description:passage)

}
