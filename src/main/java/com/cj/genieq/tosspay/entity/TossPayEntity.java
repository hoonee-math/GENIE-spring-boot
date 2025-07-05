package com.cj.genieq.tosspay.entity;

import com.cj.genieq.payment.entity.PaymentEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tosspay") // ✅ 테이블명 소문자
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TossPayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ MariaDB에서는 IDENTITY
    @Column(name = "toss_id") // ✅ 컬럼명 소문자
    private Long tossId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "toss_paycode", nullable = false) // ✅ FK도 소문자
    private PaymentEntity payment;

    @Column(name = "toss_order_id", nullable = false)
    private String orderId;

    @Column(name = "toss_payment_key", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String paymentKey;

    @Column(name = "toss_payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "toss_payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "toss_requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "toss_approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "toss_totalamount", nullable = false)
    private Integer totalAmount;
}
