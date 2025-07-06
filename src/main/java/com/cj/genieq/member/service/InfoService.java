package com.cj.genieq.member.service;

import com.cj.genieq.member.dto.response.MemberInfoResponseDto;

public interface InfoService {
    MemberInfoResponseDto getMemberInfo(Long memCode);
    int getUsageBalance(Long memCode);
    int getUsageTotal(Long memCode);
    void updateName(String memName, Long memCode);
    void updateType(String memType, Long memCode);
    void updatePassword(String currentPassword, String newPassword, String confirmPassword, Long memCode);
}
