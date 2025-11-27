package com.cj.genieq.passage.service;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.member.repository.MemberRepository;
import com.cj.genieq.passage.dto.DescriptionDto;
import com.cj.genieq.passage.dto.request.*;
import com.cj.genieq.passage.dto.response.*;
import com.cj.genieq.passage.entity.DescriptionEntity;
import com.cj.genieq.passage.entity.PassageEntity;
import com.cj.genieq.passage.repository.DescriptionRepository;
import com.cj.genieq.passage.repository.PassageRepository;
import com.cj.genieq.question.dto.request.QuestionInsertRequestDto;
import com.cj.genieq.question.dto.request.QuestionUpdateRequestDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import com.cj.genieq.question.entity.QuestionEntity;
import com.cj.genieq.question.repository.QuestionRepository;
import com.cj.genieq.question.service.QuestionService;
import com.cj.genieq.usage.service.UsageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassageServiceImpl implements PassageService {

    private final PassageRepository passageRepository;
    private final MemberRepository memberRepository;
    private final DescriptionRepository descriptionRepository;
    private final UsageService usageService;
    private final QuestionService questionService;

    // 지문 저장
    @Override
    @Transactional
    public PassageSelectResponseDto savePassage(Long memCode, PassageInsertRequestDto passageDto) {
        try {
            // 회원 조회
            MemberEntity member = memberRepository.findById(memCode)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
            
             // 제목 중복 처리
            String title = generateTitle(passageDto.getTitle());

            // Passage 엔티티 생성 및 저장
            PassageEntity passage = PassageEntity.builder()
                    .isDeleted(0)
                    .isFavorite(0)
                    .title(title)
                    .content(passageDto.getContent())
                    .date(LocalDateTime.now())
                    .isGenerated(passageDto.getIsGenerated())
                    .isUserEntered(passageDto.getIsUserEntered())
                    .refPasCode(null) // 기본 지문은 refPasCode가 null
                    .member(member)
                    .build();

            // Passage 먼저 저장
            PassageEntity savedPassage = passageRepository.save(passage);

            // Description 엔티티들 생성 및 저장
            List<DescriptionEntity> descriptions = passageDto.getDescriptions().stream()
                    .map(desc -> DescriptionEntity.builder()
                            .pasType(desc.getPasType())
                            .keyword(desc.getKeyword())
                            .gist(desc.getGist())
                            .order(desc.getOrder())
                            .passage(savedPassage)
                            .build())
                    .collect(Collectors.toList());

            // 5. Description들 저장
            descriptionRepository.saveAll(descriptions);

            // 6. 사용량 업데이트 (기존과 동일)
            usageService.updateUsage(memCode, -1, "지문 생성");


            // 7. 응답 DTO 생성 (Description 리스트 포함)
            List<DescriptionDto> descriptionDtos = descriptions.stream()
                    .map(desc -> DescriptionDto.builder()
                            .pasType(desc.getPasType())
                            .keyword(desc.getKeyword())
                            .gist(desc.getGist())
                            .order(desc.getOrder())
                            .build())
                    .collect(Collectors.toList());

            PassageSelectResponseDto selectedPassage =   PassageSelectResponseDto.builder()
                    .pasCode(savedPassage.getPasCode())
                    .title(savedPassage.getTitle())
                    .content(savedPassage.getContent())
                    .descriptions(descriptionDtos)
                    .build();

            return selectedPassage;
        } catch (EntityNotFoundException e) {
            // 회원이 없으면 EntityNotFoundException 예외 처리
            throw new EntityNotFoundException("지문 저장 실패: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // 데이터 무결성 오류 처리
            throw new DataIntegrityViolationException("데이터 무결성 위반: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();  // 예외 로깅
            return null;
        }
    }

    // 지문 수정 PATCH 일부 데이터 수정 비즈니스 로직
    @Override
    @Transactional
    public boolean updatePassagePartial(Long memCode, Long pasCode, PassagePartialUpdateRequestDto updateDto) {
        try {
            // 지문 조회 및 권한 확인
            PassageEntity passage = passageRepository.findByPasCodeAndMember_MemCode(pasCode, memCode)
                    .orElseThrow(() -> new EntityNotFoundException("지문을 찾을 수 없거나 권한이 없습니다."));

            // null이 아닌 필드만 업데이트 (JPA Dirty Checking 활용)
            if (updateDto.getTitle() != null) {
                passage.setTitle(updateDto.getTitle());
            }

            if (updateDto.getContent() != null) {
                passage.setContent(updateDto.getContent());
            }

            if (updateDto.getIsFavorite() != null) {
                passage.setIsFavorite(updateDto.getIsFavorite());
            }

            // JPA가 자동으로 변경된 필드만 UPDATE 쿼리 실행
            // repository.save() 호출 불필요 (@Transactional + Dirty Checking)

            return true;
        } catch (Exception e) {
            log.error("지문 부분 수정 실패: {}", e.getMessage());
            return false;
        }
    }

    // 지문 미리보기 리스트
    @Override
    public List<PassagePreviewListDto> getPreviewList(Long memCode) {
        // 1. 기본 지문 정보 조회
        List<PassagePreviewListDto> previews = passageRepository.findPassagePreviewsByMember(memCode, null);

        if (previews.isEmpty()) {
            throw new EntityNotFoundException("지문이 존재하지 않습니다.");
        }

        // 2. pasCode 목록 추출
        List<Long> pasCodes = previews.stream()
                .map(PassagePreviewListDto::getPasCode)
                .collect(Collectors.toList());

        // 3. Description 정보 조회
        List<DescriptionEntity> descriptions = passageRepository.findDescriptionsByPassageCodes(pasCodes);

        // 4. Description을 pasCode별로 그룹화
        Map<Long, List<DescriptionDto>> descriptionMap = descriptions.stream()
                .collect(Collectors.groupingBy(
                        desc -> desc.getPassage().getPasCode(),
                        // DescriptionEntity를 DescriptionDto로 변환하는 헬퍼 메소드 호출
                        Collectors.mapping(this::convertToDescriptionDto, Collectors.toList())
                ));

        // 5. 각 preview에 Description 매핑
        previews.forEach(preview -> {
            List<DescriptionDto> descList = descriptionMap.getOrDefault(preview.getPasCode(), new ArrayList<>());
            preview.setDescriptions(descList);
        });

        return previews;
    }
    // DescriptionEntity를 DescriptionDto로 변환하는 헬퍼 메소드
    private DescriptionDto convertToDescriptionDto(DescriptionEntity entity) {
        return DescriptionDto.builder()
                .pasType(entity.getPasType())
                .keyword(entity.getKeyword())
                .gist(entity.getGist())
                .order(entity.getOrder())
                .build();
    }

    // 지문 미리보기 즐겨찾기 리스트
    @Override
    public List<PassagePreviewListDto> getPreviewFavoriteList(Long memCode) {
        List<PassagePreviewListDto> previews = passageRepository.findPassagePreviewsByMember(memCode, 1);

        if (previews.isEmpty()) {
            throw new EntityNotFoundException("지문이 존재하지 않습니다.");
        }

        return previews;
    }

    // 지문 개별 조회
    @Override
    public PassageSelectResponseDto selectPassage(Long memCode, Long pasCode) {
//        PassageEntity passageEntity = passageRepository.findById(pasCode)
//                .orElseThrow(() -> new IllegalArgumentException("지문이 존재하지 않습니다."));
        try {
            // 지문 조회 및 권한 확인
            PassageEntity passageEntity = passageRepository.findByPasCodeAndMember_MemCode(pasCode, memCode)
                    .orElseThrow(() -> new EntityNotFoundException("지문을 찾을 수 없거나 권한이 없습니다."));

            // 2. 연관된 Description들 조회 (순서대로)
            List<DescriptionEntity> descriptions = descriptionRepository.findByPassage_PasCodeOrderByOrderAsc(pasCode);

            // 3. DescriptionEntity -> DescriptionDto 변환
            List<DescriptionDto> descriptionDtos = descriptions.stream()
                    .map(desc -> DescriptionDto.builder()
                            .pasType(desc.getPasType())
                            .keyword(desc.getKeyword())
                            .gist(desc.getGist())
                            .order(desc.getOrder())
                            .build())
                    .collect(Collectors.toList());

            // 4. 응답 DTO 생성
            PassageSelectResponseDto passage =  PassageSelectResponseDto.builder()
                    .pasCode(passageEntity.getPasCode())
                    .title(passageEntity.getTitle())
                    .content(passageEntity.getContent())
                    .descriptions(descriptionDtos)  // Description 리스트 포함
                    .build();

            return passage;
        } catch (Exception e) {
            log.error("지문 조회 실패: {}", e.getMessage());
            return null;
        }

    }

    @Override
    @Transactional
    public PassageFavoriteResponseDto favoritePassage(PassageFavoriteRequestDto requestDto){
        PassageEntity passage = passageRepository.findById(requestDto.getPasCode())
                .orElseThrow(() -> new IllegalArgumentException("지문이 존재하지 않습니다."));

        //상태
        passage.setIsFavorite(passage.getIsFavorite() == 1 ? 0 : 1); //현재 값이 1이면 0(즐겨찾기 해제) / 0이면 1(즐겨찾기 추가)
        passageRepository.save(passage);

        return  PassageFavoriteResponseDto.builder()
                .pasCode(passage.getPasCode())
                .isFavorite(passage.getIsFavorite())
                .build();
    }
    
    // 제목 중복 처리 메소드
    private String generateTitle(String title){
        // 제목 중복 처리
        String originalTitle = title;
        int counter = 1;

        // 제목 중복 체크
        while (passageRepository.existsByTitle(title)) {
            // 제목이 이미 존재하면 (1), (2), (3)... 형식으로 수정
            title = originalTitle + "(" + counter + ")";
            counter++;
        }

        return title;
    }

    // 지문 + 문항 저장 (트랜잭션 적용)
    @Transactional
    public PassageWithQuestionsResponseDto savePassageWithQuestions(Long memCode, PassageWithQuestionsRequestDto requestDto) {
        try {
            // 1. 회원 조회
            MemberEntity member = memberRepository.findById(memCode)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    
            // 2. 제목 중복 처리
            String title = generateTitle(requestDto.getTitle());
            
            PassageEntity savedPassage;
            
            // 3. refPasCode 분기 처리
            if (requestDto.getRefPasCode() == null) {
                // 3-1. 사용자 입력 지문: 먼저 기본 지문 저장
                PassageInsertRequestDto passageDto = PassageInsertRequestDto.builder()
                        .title(title)
                        .content(requestDto.getContent())
                        .isGenerated(requestDto.getIsGenerated())
                        .isUserEntered(requestDto.getIsUserEntered())
                        .descriptions(requestDto.getDescriptions())
                        .build();

                PassageSelectResponseDto basicPassage = savePassage(memCode, passageDto);
                
                // 3-2. 저장된 pasCode를 refPasCode로 사용하여 지문+문항 저장
                PassageEntity passage = PassageEntity.builder()
                        .title(title)
                        .content(requestDto.getContent())
                        .date(LocalDateTime.now())
                        .isDeleted(0)
                        .isFavorite(0)
                        .isGenerated(0)
                        .refPasCode(basicPassage.getPasCode()) // 생성된 pasCode를 refPasCode로 설정
                        .member(member)
                        .build();
                        
                savedPassage = passageRepository.save(passage);
            } else {
                // 3-3. 자료실 지문: refPasCode가 이미 있는 경우
                PassageEntity passage = PassageEntity.builder()
                        .title(title)
                        .content(requestDto.getContent())
                        .date(LocalDateTime.now())
                        .isDeleted(0)
                        .isFavorite(0)
                        .isGenerated(0)
                        .refPasCode(requestDto.getRefPasCode())
                        .member(member)
                        .build();
                        
                savedPassage = passageRepository.save(passage);
            }
    
            // 4. Description 엔티티들 생성 및 저장 (null 체크 포함)
            List<DescriptionEntity> savedDescriptions = new ArrayList<>();
            if (requestDto.getDescriptions() != null && !requestDto.getDescriptions().isEmpty()) {
                List<DescriptionEntity> descriptions = requestDto.getDescriptions().stream()
                        .map(desc -> DescriptionEntity.builder()
                                .pasType(desc.getPasType())
                                .keyword(desc.getKeyword())
                                .gist(desc.getGist())
                                .order(desc.getOrder() != null ? desc.getOrder() : 1)
                                .passage(savedPassage)
                                .build())
                        .collect(Collectors.toList());
                
                savedDescriptions = descriptionRepository.saveAll(descriptions);
            }
    
            // 5. 문항 저장은 QuestionService에서 처리
            List<QuestionSelectResponseDto> questions = new ArrayList<>();
            if (requestDto.getQuestions() != null && !requestDto.getQuestions().isEmpty()) {
                questions = questionService.saveQuestions(savedPassage, requestDto.getQuestions());
            }
    
            // 6. 사용량 처리
            usageService.updateUsage(memCode, -1, "문항 생성");
    
            // 7. Description 엔티티를 DTO로 변환
            List<DescriptionDto> descriptionDtos = savedDescriptions.stream()
                    .map(desc -> DescriptionDto.builder()
                            .pasType(desc.getPasType())
                            .keyword(desc.getKeyword())
                            .gist(desc.getGist())
                            .order(desc.getOrder())
                            .build())
                    .collect(Collectors.toList());
    
            // 8. 응답 DTO 생성
            PassageWithQuestionsResponseDto responseDto = PassageWithQuestionsResponseDto.builder()
                    .pasCode(savedPassage.getPasCode())
                    .title(savedPassage.getTitle())
                    .content(savedPassage.getContent())
                    .descriptions(descriptionDtos)
                    .questions(questions)
                    .build();
    
            return responseDto;
            
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("지문 저장 실패: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("데이터 무결성 위반: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("지문 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 지문 + 문항 조회
    @Transactional(readOnly = true)
    public PassageWithQuestionsResponseDto getPassageWithQuestions(Long pasCode) {
        // 1. 지문 + 문항 조회 (JOIN 처리)
        PassageEntity passage = passageRepository.findById(pasCode)
                .orElseThrow(() -> new IllegalArgumentException("지문이 존재하지 않습니다."));

        // 2. 엔티티 → DTO 변환
        // 지문+문항 조회하는 작업은 지문엔티티에 의존하므로 지문 서비스의 책임에 해당해서 문항 서비스에 메서드를 추가하지 않은 것이다.
        List<QuestionSelectResponseDto> questions = (passage.getQuestions() != null)
                ? passage.getQuestions().stream()
                .map(q -> QuestionSelectResponseDto.builder()
                        .queCode(q.getQueCode())
                        .queQuery(q.getQueQuery())
                        .queOption(q.getQueOption())
                        .queAnswer(q.getQueAnswer())
                        .queDescription(q.getQueDescription())
                        .queSubpassage(q.getQueSubpassage())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        // 3. DescriptionEntity -> DescriptionDto 변환
        List<DescriptionEntity> descriptions = descriptionRepository.findByPassage_PasCodeOrderByOrderAsc(pasCode);
        List<DescriptionDto> descriptionDtos = descriptions.stream()
                .map(desc -> DescriptionDto.builder()
                        .pasType(desc.getPasType())
                        .keyword(desc.getKeyword())
                        .gist(desc.getGist())
                        .order(desc.getOrder())
                        .build())
                .collect(Collectors.toList());

        // 4. 응답 DTO 생성 후 반환 값 변수에 저장
        PassageWithQuestionsResponseDto result = PassageWithQuestionsResponseDto.builder()
                .pasCode(passage.getPasCode())
                .title(passage.getTitle())
                .content(passage.getContent())
                .descriptions(descriptionDtos)
                .questions(questions) // 문항이 없을 경우 빈 리스트 반환
                .build();

        // 4. 변수 반환
        return result;
    }


    //지문 수정 + 문항 수정
    @Transactional
    public PassageWithQuestionsResponseDto updatePassage(Long memCode, Long pasCode, PassageWithQuestionsRequestDto requestDto) {
        try {
            // 1. 회원 조회
            MemberEntity member = memberRepository.findById(memCode)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    
            // 2. 기존 지문 조회
            PassageEntity passage = passageRepository.findById(pasCode)
                    .orElseThrow(() -> new IllegalArgumentException("지문이 존재하지 않습니다."));
    
            // 3. 지문 기본 필드 수정 (null 값 무시)
            if (requestDto.getTitle() != null) {
                passage.setTitle(requestDto.getTitle());
            }
            if (requestDto.getContent() != null) {
                passage.setContent(requestDto.getContent());
            }
            if (requestDto.getIsGenerated() != null) {
                passage.setIsGenerated(requestDto.getIsGenerated());
            }
            passage.setDate(LocalDateTime.now());
    
            // 4. 지문 저장
            passageRepository.save(passage);
    
            // 5. Description 업데이트 (기존 삭제 후 새로 생성)
            List<DescriptionEntity> savedDescriptions = new ArrayList<>();
            if (requestDto.getDescriptions() != null && !requestDto.getDescriptions().isEmpty()) {
                // 기존 Description 삭제
                List<DescriptionEntity> existingDescriptions = descriptionRepository.findByPassage_PasCodeOrderByOrderAsc(pasCode);
                if (!existingDescriptions.isEmpty()) {
                    descriptionRepository.deleteAll(existingDescriptions);
                }
    
                // 새로운 Description 생성 및 저장
                List<DescriptionEntity> newDescriptions = requestDto.getDescriptions().stream()
                        .map(desc -> DescriptionEntity.builder()
                                .pasType(desc.getPasType())
                                .keyword(desc.getKeyword())
                                .gist(desc.getGist())
                                .order(desc.getOrder() != null ? desc.getOrder() : 1)
                                .passage(passage)
                                .build())
                        .collect(Collectors.toList());
    
                savedDescriptions = descriptionRepository.saveAll(newDescriptions);
            }
    
            // 6. 문항 수정 (Questions가 있는 경우에만)
            List<QuestionSelectResponseDto> updatedQuestions = new ArrayList<>();
            if (requestDto.getQuestions() != null && !requestDto.getQuestions().isEmpty()) {
                // INSERT → UPDATE 변환 처리
                List<QuestionUpdateRequestDto> questionDtos = requestDto.getQuestions().stream()
                        .map(q -> QuestionUpdateRequestDto.builder()
                                .queCode(q.getQueCode())
                                .queQuery(q.getQueQuery())
                                .queOption(q.getQueOption())
                                .queAnswer(q.getQueAnswer())
                                .queDescription(q.getQueDescription())
                                .build())
                        .collect(Collectors.toList());
    
                // 문항 수정 후 반환된 값 받아서 사용
                updatedQuestions = questionService.updateQuestions(passage, questionDtos);
            }
    
            // 7. 사용량 처리 (mode가 "generate" 또는 "recreate"일 때만 차감)
            if ("generate".equals(requestDto.getMode()) || "recreate".equals(requestDto.getMode())) {
                usageService.updateUsage(memCode, -1, "문항 생성");
            }

            // 8. Description 엔티티를 DTO로 변환
            List<DescriptionDto> descriptionDtos = savedDescriptions.stream()
                    .map(desc -> DescriptionDto.builder()
                            .pasType(desc.getPasType())
                            .keyword(desc.getKeyword())
                            .gist(desc.getGist())
                            .order(desc.getOrder())
                            .build())
                    .collect(Collectors.toList());
    
            // 9. 응답 DTO 생성
            PassageWithQuestionsResponseDto responseDto = PassageWithQuestionsResponseDto.builder()
                    .pasCode(passage.getPasCode())
                    .title(passage.getTitle())
                    .content(passage.getContent())
                    .descriptions(descriptionDtos)
                    .questions(updatedQuestions)
                    .build();
    
            return responseDto;
    
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("지문 수정 실패: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 요청: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("데이터 무결성 위반: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("지문 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 자료실 메인화면 리스트(즐겨찾기+최근 작업)
    @Override
    public List<PassageStorageEachResponseDto> selectPassageListInStorage(Long memCode, Integer isFavorite, Integer rownum) {
        List<PassageEntity> passageEntities = passageRepository.selectPassageListInStorage(memCode, isFavorite, rownum);

        // 조회 결과가 없으면 빈 리스트 반환
        if (passageEntities == null || passageEntities.isEmpty()) {
            return Collections.emptyList();
        }

        List<PassageStorageEachResponseDto> passages = passageEntities.stream()
                .map(p -> PassageStorageEachResponseDto.builder()
                        .title(p.getTitle())
                        .isGenerated(p.getIsGenerated())
                        .date(p.getDate())
                        .isFavorite(p.getIsFavorite())
                        .build())
                .collect(Collectors.toList());

        return passages;
    }

    // 즐겨찾기 리스트
    @Override
    public List<PassageStorageEachResponseDto> selectFavoriteList(Long memCode) {
        List<PassageEntity> passageEntities = passageRepository.selectTop150FavoritePassages(memCode);

        // 조회 결과가 없으면 빈 리스트 반환
        if (passageEntities == null || passageEntities.isEmpty()) {
            return Collections.emptyList();
        }

        List<PassageStorageEachResponseDto> passages = passageEntities.stream()
                .filter(p -> p.getIsDeleted() == 0) // isDeleted = 0 필터링
                .map(p -> PassageStorageEachResponseDto.builder()
                        .pasCode(p.getPasCode())
                        .title(p.getTitle())
                        .isGenerated(p.getIsGenerated())
                        .date(p.getDate())
                        .isFavorite(p.getIsFavorite())
                        .build())
                .collect(Collectors.toList());

        return passages;
    }

    // 최근 작업 내역 리스트 (구 버전의 Storage, WorkListMain 에서 사용하는 api)
    @Override
    public List<PassageStorageEachResponseDto> selectRecentList(Long memCode) {
        List<PassageEntity> passageEntities = passageRepository.selectTop150RecentPassages(memCode);
        System.out.println("받아온 데이터 크기:"+passageEntities.toArray().length);

        // 조회 결과가 없으면 빈 리스트 반환
        if (passageEntities == null || passageEntities.isEmpty()) {
            return Collections.emptyList();
        }

        List<PassageStorageEachResponseDto> passages = passageEntities.stream()
                .filter(p -> p.getIsDeleted() == 0) // isDeleted = 0 필터링
                .map(p -> PassageStorageEachResponseDto.builder()
                        .pasCode(p.getPasCode())
                        .title(p.getTitle())
                        .isGenerated(p.getIsGenerated())
                        .date(p.getDate())
                        .isFavorite(p.getIsFavorite())
                        .build())
                .collect(Collectors.toList());

        return passages;
    }

    @Override
    public int countRecentChange(Long memCode) {

        int countRecentChange = passageRepository.countByMemberAndIsDeleted(memCode, 0);

        return countRecentChange;
    }

    // 지문 삭제
    @Transactional
    @Override
    public boolean deletePassage(PassageDeleteRequestDto requestDto) {
        List<Long> pasCodeList = requestDto.getPasCodeList();

        try {
            int updatedCount = passageRepository.updateIsDeletedByPasCodeList(pasCodeList);
            // 업데이트된 개수가 전달받은 리스트 크기와 같으면 성공
            return updatedCount == pasCodeList.size();
        } catch (Exception e) {
            // 삭제 실패 시 로그 기록
            System.err.println("삭제 실패: " + e.getMessage());
            return false;
        }
    }

    // 작업명(지문 이름) 변경
    @Transactional
    @Override
    public boolean updatePassageTitle(PassageUpdateTitleRequestDto requestDto) {
        if (requestDto.getTitle() == null || requestDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException("수정할 제목이 없습니다.");
        }

        // 수정할 대상이 있는지 먼저 확인
        PassageEntity passage = passageRepository.findById(requestDto.getPasCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 지문이 존재하지 않습니다."));

        // 같은 제목이면 쿼리 실행 방지
        if (passage.getTitle().equals(requestDto.getTitle())) {
            return false;
        }

        // 제목 중복 처리
        String title = generateTitle(requestDto.getTitle());

        // 수정 실행
        int updatedCount = passageRepository.updateTitleByPasCode(requestDto.getPasCode(),title);

        // 수정이 실패
        if (updatedCount == 0) {
            throw new IllegalStateException("지문 제목 수정에 실패했습니다.");
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassageWithQuestionsResponseDto> getPassagesWithQuestionsList(Long memCode) {
        // 1. Repository에서 문항이 있는 지문들 조회 (기본 정보만)
        List<PassageEntity> passages = passageRepository.findPassagesWithQuestionsByMember(memCode);
        System.out.println("조회한 지문 목록: "+passages.size());
        if (passages == null || passages.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 각 지문에 대해 상세 정보 조회 (기존 메서드 재사용)
        List<PassageWithQuestionsResponseDto> result = passages.stream()
                .map(passage -> {
                    try {
                        // 기존 getPassageWithQuestions 메서드 재사용하여 상세 정보 조회
                        return getPassageWithQuestions(passage.getPasCode());
                    } catch (Exception e) {
                        log.warn("지문 {}의 상세 정보 조회 실패: {}", passage.getPasCode(), e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null && dto.getQuestions() != null && !dto.getQuestions().isEmpty())
                .collect(Collectors.toList());

        return result;
    }
}
