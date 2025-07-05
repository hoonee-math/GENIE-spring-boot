package com.cj.genieq.ticket.entity;

import com.cj.genieq.payment.entity.PaymentEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@Table(name = "ticket") // ✅ 테이블 이름 소문자
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ MariaDB용 ID 생성 방식
    @Column(name = "tic_code")
    private Long ticCode;

    @Column(name = "tic_number")
    private Integer ticNumber;

    @Column(name = "tic_price")
    private Integer price;

    @JsonIgnore
    @OneToMany(mappedBy = "ticket")
    private List<PaymentEntity> payments = new ArrayList<>();
}
