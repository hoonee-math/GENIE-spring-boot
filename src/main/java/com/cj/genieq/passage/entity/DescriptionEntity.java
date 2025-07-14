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
    private String pasType;

    @Column(name = "pas_keyword")
    private String keyword;

    @Lob
    @Column(name = "pas_gist", columnDefinition = "LONGTEXT")
    private String gist;

    @Column(name = "pas_order")
    @Builder.Default
    private int order=1;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pas_code") // ✅ FK도 소문자
    private PassageEntity passage;

}
