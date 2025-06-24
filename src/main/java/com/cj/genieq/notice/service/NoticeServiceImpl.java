package com.cj.genieq.notice.service;

import com.cj.genieq.notice.dto.response.NoticeListResponseDto;
import com.cj.genieq.notice.dto.response.NoticeResponseDto;
import com.cj.genieq.notice.entity.NoticeEntity;
import com.cj.genieq.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;

    @Override
    public List<NoticeListResponseDto> getNoticeList() {

        List<NoticeEntity> entities = noticeRepository.findAllByOrderByDateDesc();

        List<NoticeListResponseDto> result = entities.stream()
                .map(entity->NoticeListResponseDto.builder()
                        .notCode(entity.getNotCode())
                        .type(entity.getType())
                        .title(entity.getTitle())
                        .date(entity.getDate())
                        .build())
                .toList();

        return result;
    }

    @Override
    public NoticeResponseDto getNotice(Long notCode) {
        NoticeEntity entity = noticeRepository.findById(notCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 없습니다."));

        NoticeResponseDto notice = NoticeResponseDto.builder()
                .notCode(entity.getNotCode())
                .title(entity.getTitle())
                .content(entity.getContent())
                .date(entity.getDate())
                .type(entity.getType())
                .build();

        return notice;
    }
}
