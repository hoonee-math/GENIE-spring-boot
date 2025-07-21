package com.cj.genieq.question.service;

import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.question.dto.request.QuestionUpdateRequestDto;
import com.cj.genieq.question.dto.request.QuestionInsertRequestDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import com.cj.genieq.question.entity.QuestionEntity;
import com.cj.genieq.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;

    // 문항 저장 로직 이동
    public List<QuestionSelectResponseDto> saveQuestions(PassageEntity savedPassage, List<QuestionInsertRequestDto> questions) {
        List<QuestionEntity> questionEntities = questions.stream()
                .map(q -> QuestionEntity.builder()
                        .queQuery(q.getQueQuery())
                        .queOption(q.getQueOption())
                        .queAnswer(q.getQueAnswer())
                        .queDescription(q.getDescription())
                        .passage(savedPassage) // 지문 코드 매핑
                        .build())
                .collect(Collectors.toList());

        //  저장 후 저장된 값 반환
        List<QuestionEntity> savedQuestions = questionRepository.saveAll(questionEntities);

        //  반환 DTO 변환
        return savedQuestions.stream()
                .map(q -> QuestionSelectResponseDto.builder()
                        .queCode(q.getQueCode())
                        .queQuery(q.getQueQuery())
                        .queOption(q.getQueOption())
                        .queAnswer(q.getQueAnswer())
                        .description(q.getQueDescription())
                        .build())
                .collect(Collectors.toList());
    }

    //기존 문항 삭제 후 새 문항 저장
    public List<QuestionSelectResponseDto> updateQuestions(PassageEntity passage, List<QuestionUpdateRequestDto> questions) {
        // 기존 문항 삭제
        questionRepository.deleteByPassage(passage);

        // 새 문항 저장
        List<QuestionEntity> newQuestions = questions.stream()
                .map(q -> QuestionEntity.builder()
                        .queQuery(q.getQueQuery())
                        .queOption(q.getQueOption())
                        .queAnswer(q.getQueAnswer())
                        .passage(passage) // 지문 매핑
                        .queDescription(q.getDescription())
                        .build())
                .collect(Collectors.toList());

        List<QuestionEntity> updatedQuestions = questionRepository.saveAll(newQuestions);

        // 저장된 값 반환
        return updatedQuestions.stream()
                .map(q -> QuestionSelectResponseDto.builder()
                        .queCode(q.getQueCode())
                        .queQuery(q.getQueQuery())
                        .queOption(q.getQueOption())
                        .queAnswer(q.getQueAnswer())
                        .description(q.getQueDescription())
                        .build())
                .collect(Collectors.toList());

    }

}
