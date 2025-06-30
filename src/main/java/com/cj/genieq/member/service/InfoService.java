package com.cj.genieq.member.service;

import com.cj.genieq.member.dto.request.UpdateNameRequestDto;
import com.cj.genieq.member.dto.response.MemberInfoResponseDto;


public interface InfoService {
    MemberInfoResponseDto getMemberInfo(Long memCode);
    int getUsageBalance(Long memCode);
    int getUsageTotal(Long memCode);
    void updateName(String memName, com.cj.genieq.member.entity.MemberEntity member);
    void updateType(String memType, com.cj.genieq.member.entity.MemberEntity member);
    void updatePassword(String currentPassword, String newPassword, String confirmPassword, com.cj.genieq.member.entity.MemberEntity member);
}
