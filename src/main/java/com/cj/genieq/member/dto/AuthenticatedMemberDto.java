package com.cj.genieq.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 인증된 사용자 정보 DTO
 * Spring Security의 @AuthenticationPrincipal로 사용되는 경량화된 사용자 정보
 * LazyInitializationException 방지를 위해 연관관계 필드 제외
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class AuthenticatedMemberDto {

    private Long memCode;       /* 회원 고유 코드 (Primary Key) */
    private String memName;
    private String memEmail;
    private String role;        /* 사용자 권한 (ROLE_USER, ROLE_ADMIN 등) */
    private Boolean enabled;    /* 계정 활성화 여부 */
}
