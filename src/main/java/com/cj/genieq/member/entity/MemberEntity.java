package com.cj.genieq.member.entity;

import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.payment.entity.PaymentEntity;
import com.cj.genieq.usage.entity.UsageEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member") // ✅ 테이블 이름 소문자
@Data
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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<UsageEntity> usages = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PassageEntity> passages = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PaymentEntity> payments = new ArrayList<>();
}
