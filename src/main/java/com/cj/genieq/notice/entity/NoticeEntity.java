package com.cj.genieq.notice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Entity
@Table(name = "notice") // ✅ 소문자 테이블명
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ 시퀀스 제거, MariaDB 방식
    @Column(name = "not_code") // ✅ 소문자로 통일
    private Long notCode;

    @Column(name = "not_type")
    private String type;

    @Column(name = "not_title")
    private String title;

    @Lob
    @Column(name = "not_content")
    private String content;

    @Column(name = "not_date")
    private LocalDate date;
}
