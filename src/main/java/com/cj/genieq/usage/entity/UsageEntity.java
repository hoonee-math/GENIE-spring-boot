package com.cj.genieq.usage.entity;

import com.cj.genieq.member.entity.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Entity
@DynamicInsert
@Table(name = "usage_log") // ✅ 예약어 피하고 소문자 테이블명
public class UsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ MariaDB ID 생성 방식
    @Column(name = "usa_code")
    private Long usaCode;

    @Column(name = "usa_type")
    private String usaType;

    @Column(name = "usa_date")
    private LocalDateTime usaDate;

    @Column(name = "usa_count")
    private int usaCount;

    @Column(name = "usa_balance")
    private int usaBalance;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "mem_code") // ✅ FK도 소문자
    private MemberEntity member;

    @Override
    public String toString() {
        return "UsageEntity{" +
                "usaCode=" + usaCode +
                ", usaType='" + usaType + '\'' +
                ", usaDate=" + usaDate +
                ", usaCount=" + usaCount +
                ", usaBalance=" + usaBalance +
                '}';
    }
}
