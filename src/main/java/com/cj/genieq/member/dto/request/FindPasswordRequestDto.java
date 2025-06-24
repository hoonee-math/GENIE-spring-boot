package com.cj.genieq.member.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordRequestDto {
    private String memEmail;
    private String tempPassword;  // 임시 비밀번호
}
