package com.cj.genieq.question.service;

import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.question.dto.request.QuestionInsertRequestDto;
import com.cj.genieq.question.dto.request.QuestionUpdateRequestDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import com.cj.genieq.question.entity.QuestionEntity;

import java.util.List;

public interface QuestionService {
    List<QuestionSelectResponseDto> saveQuestions(PassageEntity savedPassage, List<QuestionInsertRequestDto> questions);
    List<QuestionSelectResponseDto> updateQuestions(PassageEntity passage, List<QuestionUpdateRequestDto> questions);
    QuestionEntity addQuestionToExistingPassage(Long memCode, Long pasCode, QuestionInsertRequestDto requestDto);
}