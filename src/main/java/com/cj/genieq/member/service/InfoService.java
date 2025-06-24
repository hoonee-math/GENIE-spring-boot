package com.cj.genieq.member.service;

import com.cj.genieq.member.dto.request.UpdateNameRequestDto;
import com.cj.genieq.member.dto.response.MemberInfoResponseDto;
import jakarta.servlet.http.HttpSession;

public interface InfoService {
    MemberInfoResponseDto getMemberInfo(Long memCode);
    int getUsageBalance(Long memCode);
    int getUsageTotal(Long memCode);
    void updateName(String memName, HttpSession session);
    void updateType(String memType, HttpSession session);
    void updatePassword(String currentPassword, String newPassword, String confirmPassword, HttpSession session);
}
