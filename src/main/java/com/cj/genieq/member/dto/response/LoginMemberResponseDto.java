package com.cj.genieq.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class LoginMemberResponseDto {
    private Long memberCode;
    private String name;
    private String email;
}
