package com.cj.genieq.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDto {
    //회원가입 기능에서 필요한 컬럼만 추려서 정의
    private String memName;
    private String memEmail;
    private String memPassword;
    private String memGender;
    private String memType;
}
