package com.cj.genieq.notice.controller;

import com.cj.genieq.notice.dto.response.NoticeListResponseDto;
import com.cj.genieq.notice.dto.response.NoticeResponseDto;
import com.cj.genieq.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/noti")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping("/select/list")
    public ResponseEntity<?> select() {
        List<NoticeListResponseDto> notices = noticeService.getNoticeList();

        return ResponseEntity.ok().body(notices);
    }

    @GetMapping("/select/each")
    public ResponseEntity<?> selectEach(@RequestParam Long notCode){
        NoticeResponseDto notice = noticeService.getNotice(notCode);

        return ResponseEntity.ok().body(notice);
    }
}
