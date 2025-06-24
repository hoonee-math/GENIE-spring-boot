package com.cj.genieq.payment.entity;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.ticket.entity.TicketEntity;
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
@Table(name = "payment") // ✅ 소문자로 변경
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ MariaDB는 IDENTITY 사용
    @Column(name = "pay_code") // ✅ 컬럼 소문자로
    private Long payCode;

    @Column(name = "pay_price")
    private Integer price;

    @Column(name = "pay_status")
    private String status;

    @Column(name = "pay_date")
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "mem_code") // ✅ FK도 소문자
    private MemberEntity member;

    @ManyToOne
    @JoinColumn(name = "tic_code") // ✅ FK도 소문자
    private TicketEntity ticket;
}
