package com.cj.genieq.passage.service;

import com.cj.genieq.passage.dto.request.PassageFavoriteRequestDto;
import com.cj.genieq.passage.dto.request.PassageInsertRequestDto;
import com.cj.genieq.passage.dto.request.PassageUpdateRequestDto;
import com.cj.genieq.passage.dto.request.PassageWithQuestionsRequestDto;
import com.cj.genieq.passage.dto.response.PassageFavoriteResponseDto;
import com.cj.genieq.passage.dto.response.PassageSelectResponseDto;
import com.cj.genieq.passage.dto.response.PassagePreviewListDto;
import com.cj.genieq.passage.dto.response.PassageWithQuestionsResponseDto;
import com.cj.genieq.passage.dto.request.*;
import com.cj.genieq.passage.dto.response.*;
import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.question.dto.request.QuestionInsertRequestDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PassageService {
    PassageSelectResponseDto savePassage(Long memCode, PassageInsertRequestDto passageDto);
    PassageFavoriteResponseDto favoritePassage(PassageFavoriteRequestDto requestDto);
    boolean updatePassage(PassageUpdateRequestDto passageDto);
    List<PassagePreviewListDto> getPreviewList(Long memCode);
    List<PassagePreviewListDto> getPreviewFavoriteList(Long memCode);
    PassageSelectResponseDto selectPassage(Long memCode, Long pasCode);

    PassageWithQuestionsResponseDto savePassageWithQuestions(Long memCode, PassageWithQuestionsRequestDto requestDto);
    PassageWithQuestionsResponseDto getPassageWithQuestions(Long pasCode);
    PassageWithQuestionsResponseDto updatePassage(Long memCode, Long pasCode, PassageWithQuestionsRequestDto requestDto);
    boolean updatePassagePartial(Long memCode, Long pasCode, PassagePartialUpdateRequestDto updateDto);

    List<PassageStorageEachResponseDto> selectPassageListInStorage(Long memCode, Integer isFavorite, Integer rownum);
    List<PassageStorageEachResponseDto> selectFavoriteList(Long memCode);
    List<PassageStorageEachResponseDto> selectRecentList(Long memCode);
    int countRecentChange(Long memCode);
    boolean deletePassage(PassageDeleteRequestDto requestDto);
    boolean updatePassageTitle(PassageUpdateTitleRequestDto requestDto);
    List<PassageStorageEachResponseDto> findDeletedByMember(Long memCode);
}
