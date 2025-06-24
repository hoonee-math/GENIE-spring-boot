package com.cj.genieq.member.dto.response;

import lombok.*;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class MemberInfoResponseDto {
    private Long memCode;
    private String name;
    private String email;
    private String gender;
    private String memType;
}
