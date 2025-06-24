package com.cj.genieq.notice.service;

import com.cj.genieq.notice.dto.response.NoticeListResponseDto;
import com.cj.genieq.notice.dto.response.NoticeResponseDto;

import java.util.List;

public interface NoticeService {
    List<NoticeListResponseDto> getNoticeList();
    NoticeResponseDto getNotice(Long notCode);
}
