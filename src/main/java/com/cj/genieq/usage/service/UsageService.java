package com.cj.genieq.usage.service;

import com.cj.genieq.usage.dto.response.UsageListResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface UsageService {
    List<UsageListResponseDto> getUsageList(
            Long memCode, LocalDate startDate, LocalDate endDate);

    void updateUsage(Long memCode, int count, String type);
}
