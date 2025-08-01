package com.cj.genieq.question.service;

import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.passage.repository.PassageRepository;
import com.cj.genieq.question.dto.request.QuestionPartialUpdateRequestDto;
import com.cj.genieq.question.dto.request.QuestionUpdateRequestDto;
import com.cj.genieq.question.dto.request.QuestionInsertRequestDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import com.cj.genieq.question.entity.QuestionEntity;
import com.cj.genieq.question.repository.QuestionRepository;
import com.cj.genieq.usage.service.UsageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final PassageRepository passageRepository;
    private final UsageService usageService;

    // 문항만 추가
    @Transactional
    public QuestionEntity addQuestionToExistingPassage(Long memCode, Long pasCode, QuestionInsertRequestDto requestDto) {
        try {
            // 1. 기존 지문 조회
            PassageEntity existingPassage = passageRepository.findById(pasCode)
                    .orElseThrow(() -> new EntityNotFoundException("지문을 찾을 수 없습니다: " + pasCode));

            // 2. 권한 확인 (해당 사용자의 지문인지)
            if (!existingPassage.getMember().getMemCode().equals(memCode)) {
                throw new IllegalAccessException("해당 지문에 대한 권한이 없습니다.");
            }
            QuestionEntity question = QuestionEntity.builder()
                    .queQuery(requestDto.getQueQuery())
                    .queOption(requestDto.getQueOption())
                    .queAnswer(requestDto.getQueAnswer())
                    .queDescription(requestDto.getQueDescription())
                    .queSubpassage(requestDto.getQueSubpassage())
                    .passage(existingPassage)
                    .build();

            // 4. 새로운 문항 저장
            QuestionEntity savedQuestion = questionRepository.save(question);

            // 5. 사용량 기록 (문항 생성)
            usageService.updateUsage(memCode, -1, "문항 추가");

            // 6. 업데이트된 전체 데이터 반환
            return savedQuestion;

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("권한이 없습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("문항 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 문항 저장 로직 이동
    public List<QuestionSelectResponseDto> saveQuestions(PassageEntity savedPassage, List<QuestionInsertRequestDto> questions) {
        List<QuestionEntity> questionEntities = questions.stream()
                .map(q -> QuestionEntity.builder()
                        .queQuery(q.getQueQuery())
                        .queOption(q.getQueOption())
                        .queAnswer(q.getQueAnswer())
                        .queDescription(q.getQueDescription())
                        .queSubpassage(q.getQueSubpassage())
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
                        .queDescription(q.getQueDescription())
                        .queSubpassage(q.getQueSubpassage())
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
                        .queDescription(q.getQueDescription())
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
                        .queDescription(q.getQueDescription())
                        .build())
                .collect(Collectors.toList());

    }

    // 지문 수정과 유사한 패턴으로 구현
    @Override
    @Transactional
    public boolean updateQuestionPartial(Long memCode, Long pasCode, Long queCode, QuestionPartialUpdateRequestDto updateDto) {
        try {
            // 1단계: 지문 권한 확인
            boolean hasPassagePermission = passageRepository.existsByPasCodeAndMember_MemCode(pasCode, memCode);
            if (!hasPassagePermission) {
                throw new EntityNotFoundException("지문을 찾을 수 없거나 권한이 없습니다.");
            }

            // 2단계: 문항 확인 및 로드
            QuestionEntity question = questionRepository.findByQueCodeAndPassage_PasCode(queCode, pasCode)
                    .orElseThrow(() -> new EntityNotFoundException("문항을 찾을 수 없습니다."));

            // null이 아닌 필드만 업데이트 (JPA Dirty Checking 활용)
            if (updateDto.getQueQuery() != null) {
                question.setQueQuery(updateDto.getQueQuery());
            }

            if (updateDto.getQueOption() != null) {
                question.setQueOption(updateDto.getQueOption());
            }

            if (updateDto.getQueAnswer() != null) {
                question.setQueAnswer(updateDto.getQueAnswer());
            }

            if (updateDto.getQueSubpassage() != null) {
                question.setQueSubpassage(updateDto.getQueSubpassage());
            }

            if (updateDto.getQueDescription() != null) {
                question.setQueDescription(updateDto.getQueDescription());
            }

            // JPA가 자동으로 변경된 필드만 UPDATE 쿼리 실행
            // repository.save() 호출 불필요 (@Transactional + Dirty Checking)

            return true;
        } catch (Exception e) {
            log.error("문항 부분 수정 실패: {}", e.getMessage());
            return false;
        }
    }

}
