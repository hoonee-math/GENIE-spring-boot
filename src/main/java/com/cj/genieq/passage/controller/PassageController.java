package com.cj.genieq.passage.controller;

import com.cj.genieq.member.entity.MemberEntity;
import com.cj.genieq.passage.dto.request.*;
import com.cj.genieq.passage.dto.response.*;
import com.cj.genieq.passage.repository.PassageRepository;
import com.cj.genieq.passage.service.*;
import com.cj.genieq.question.dto.request.QuestionInsertRequestDto;
import com.cj.genieq.question.dto.request.QuestionPartialUpdateRequestDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import com.cj.genieq.question.entity.QuestionEntity;
import com.cj.genieq.question.service.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataAccessException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pass")
@RequiredArgsConstructor
public class PassageController {

    private final PassageService passageService;
    private final StorageService storageService;
    private final PdfService pdfService;
    private final WordService wordService;
    private final TxtService txtService;
    private final QuestionService questionService;

    /**
     * 지문 개별 저장 API (JWT 기반)
     * 기존 세션 방식에서 JWT 토큰 기반 인증으로 전환
     * @param member JWT로 인증된 사용자 정보 (자동 주입)
     * @param passageDto 지문 생성 요청 데이터
     * @return 생성된 지문 정보 또는 에러 메시지
     */
    @PostMapping("/insert/each")
    public ResponseEntity<?> insertEach(
            @AuthenticationPrincipal Long memCode, // Spring Security가 자동으로 JWT 검증 및 사용자 정보 주입, 인증되지 않은 요청은 SecurityConfig에서 401 자동 처리
            @RequestBody PassageInsertRequestDto passageDto) {
        System.out.println("request passage data: " + passageDto.toString());

        // 지문 생성 (기존 비즈니스 로직 유지)
        PassageSelectResponseDto savedPassage = passageService.savePassage(memCode, passageDto);
    
        if (savedPassage != null) {
            return ResponseEntity.ok(savedPassage);
        } else {
            return ResponseEntity.badRequest().body("저장 실패");
        }
    }

    @GetMapping("/select/prevlist")
    public ResponseEntity<?> selectPrevList(@AuthenticationPrincipal Long memCode) {
        try {

            List<PassagePreviewListDto> previews = passageService.getPreviewList(memCode);

            // 지문 목록이 비어있는 경우 처리 (optional)
            if (previews.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("지문 목록이 없습니다.");
            }

            return ResponseEntity.ok(previews);
        } catch (EntityNotFoundException e) {
            // 예를 들어, 서비스에서 데이터가 없을 경우의 예외 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("지문 정보가 존재하지 않습니다.");
        } catch (Exception e) {
            // 예기치 못한 예외 처리
            e.printStackTrace(); // 로그로 예외 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/list/withquestions")
    public ResponseEntity<?> selectPassListWithQues(@AuthenticationPrincipal Long memCode) {
        try {
            System.out.println("지문 확인 요청 들어옴");
            List<PassageWithQuestionsResponseDto> passagesWithQuestions = passageService.getPassagesWithQuestionsList(memCode);
            System.out.println("지문 리스트 정보: "+passagesWithQuestions.size());
            if (passagesWithQuestions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("문항이 있는 지문이 없습니다.");
            }
            
            return ResponseEntity.ok(passagesWithQuestions);
        } catch (Exception e) {
            log.error("문항이 있는 지문 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 지문 개별 조회
    @GetMapping("/select/{pasCode}")
    public ResponseEntity<?> selectPassage(@AuthenticationPrincipal Long memCode, @PathVariable Long pasCode) {
        try {
            // PassageService에서 지문 정보를 조회
            PassageSelectResponseDto passage = passageService.selectPassage(memCode, pasCode);

            // 지문이 존재하지 않으면 예외 처리
            if (passage == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("지문을 찾을 수 없습니다.");
            }

            return ResponseEntity.ok(passage);

        } catch (EntityNotFoundException e) {
            // 지문이 없을 경우 예외 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("지문을 찾을 수 없습니다.");
        } catch (Exception e) {
            // 기타 예외 처리 (예기치 않은 오류)
            e.printStackTrace();  // 로깅용
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @PatchMapping("/favo")
    public ResponseEntity<PassageFavoriteResponseDto> favoritePassage(@RequestBody PassageFavoriteRequestDto requestDto) {
        PassageFavoriteResponseDto responseDto = passageService.favoritePassage(requestDto);
        return ResponseEntity.ok(responseDto);
    }


    // 지문 + 문항 저장
    @PostMapping("/ques/insert/each")
    public ResponseEntity<?> savePassage(@AuthenticationPrincipal Long memCode, @RequestBody PassageWithQuestionsRequestDto requestDto) {

        PassageWithQuestionsResponseDto responseDto = passageService.savePassageWithQuestions(memCode, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    // 기존 pasCode에 추가되는 문항을 저장
    @PostMapping("/ques/add/{pasCode}")
    public ResponseEntity<?> addQuestionToPassage(@AuthenticationPrincipal Long memCode,@PathVariable Long pasCode, @RequestBody QuestionInsertRequestDto requestDto) {
        System.out.println("request passage data: " + requestDto.toString());
        try{
            // 기존 지문에 새 문항만 추가하는 로직, 저장한 데이터의 QueCode 값을 응답해줌
            QuestionEntity responseData = questionService.addQuestionToExistingPassage(memCode, pasCode, requestDto);
            return ResponseEntity.ok(responseData);
        }catch (EntityNotFoundException e) {
            // requestDto.pasCode 가 없을 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("지문을 찾을 수 없습니다.");
        } catch (Exception e) {
            // 기타 예외 처리 (예기치 않은 오류)
            e.printStackTrace();  // 로깅용
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }

    }

    // 지문 + 문항 조회
    @GetMapping("/ques/select/{pasCode}")
    public ResponseEntity<PassageWithQuestionsResponseDto> getPassage(@PathVariable Long pasCode) {
        PassageWithQuestionsResponseDto responseDto = passageService.getPassageWithQuestions(pasCode);
        return ResponseEntity.ok(responseDto);
    }

    // put 요청 지문 수정, 문항 수정 요청 제거
    // 지문 데이터 수정 Patch 를 사용 (기존 PUT은 리소스 전체를 대체하는 반면, PATCH는 리소스의 일부만 수정)
    @PatchMapping("/{pasCode}")
    public ResponseEntity<?> updatePassagePartial(
            @AuthenticationPrincipal Long memCode,
            @PathVariable Long pasCode,
            @RequestBody PassagePartialUpdateRequestDto updateDto
    ) {
        try {
            // System.out.println("request passage data: " + updateDto.toString());
            boolean success = passageService.updatePassagePartial(memCode, pasCode, updateDto);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "수정 완료", "success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "수정 실패", "success", false));
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "지문을 찾을 수 없습니다.", "success", false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "서버 오류가 발생했습니다.", "success", false));
        }
    }

    // 문항 데이터 수정 Patch 를 사용 (PUT은 리소스 전체를 대체하는 반면, PATCH는 리소스의 일부만 수정)
    @PatchMapping("/{pasCode}/ques/{queCode}")
    public ResponseEntity<?> updateQuestionPartial(
            @AuthenticationPrincipal Long memCode,
            @PathVariable Long pasCode,
            @PathVariable Long queCode,
            @RequestBody QuestionPartialUpdateRequestDto updateDto
    ) {
        try {
            System.out.println("문항 데이터 수정 요청 들어옴, updateDto: " + updateDto.toString());
            boolean success = questionService.updateQuestionPartial(memCode, pasCode, queCode, updateDto);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "문항 수정 완료", "success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "문항 수정 실패", "success", false));
            }
        } catch (EntityNotFoundException e) {
            System.out.println("문항 찾기 실패");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "문항을 찾을 수 없습니다.", "success", false));
        } catch (Exception e) {
            System.out.println("서버 오류");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "서버 오류가 발생했습니다.", "success", false));
        }
    }

    // 자료실 메인화면 리스트(즐겨찾기+최근 작업)
    @GetMapping("/select/list")
    public ResponseEntity<?> selectList(@AuthenticationPrincipal Long memCode) {

        try {
            List<PassageStorageEachResponseDto> favorites = passageService.selectPassageListInStorage(memCode, 1, 5);
            List<PassageStorageEachResponseDto> recent = passageService.selectPassageListInStorage(memCode, 0, 8);

            PassageStorageMainResponseDto responseDto = PassageStorageMainResponseDto.builder()
                    .favorites(favorites)
                    .recent(recent)
                    .build();

            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            // 잘못된 파라미터 값 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 요청입니다: " + e.getMessage());
        } catch (DataAccessException e) {
            // DB 오류 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("데이터베이스 오류가 발생했습니다.");
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버에서 문제가 발생했습니다.");
        }
    }

    // 최근 작업 내역 리스트 (구 버전의 Storage, WorkListMain 에서 사용하는 api)
    @GetMapping("/select/recelist")
    public ResponseEntity<String> selectRecent(@AuthenticationPrincipal Long memCode) {
        List<PassageStorageEachResponseDto> recents = passageService.selectRecentList(memCode);

        // ObjectMapper에 JavaTimeModule 등록
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO 8601 형식으로 출력

        try {
            String jsonResponse = mapper.writeValueAsString(recents);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(jsonResponse);
        } catch (Exception e) {
            System.err.println("JSON 변환 오류: " + e.getMessage());
            return ResponseEntity.status(500).body("[]");
        }
    }

    /**
     * 통합 Storage 리스트 조회 엔드포인트 (새 버전의 storage 컴포넌트에서 사용하는 api)
     * GET /api/pass/storage/{type}?page=1&size=15&field=기술&search=AI&sort=date&order=desc
     */
    @GetMapping("/storage/{type}")
    public ResponseEntity<?> getStorageList(                        // @return 통합 응답 DTO (페이지네이션 포함)
            @PathVariable String type,                              // 리스트 타입 (recent, favorite, deleted)
            @RequestParam(defaultValue = "1") int page,             // 페이지 번호 (1부터 시작)
            @RequestParam(defaultValue = "15") int size,            // 페이지 크기
            @RequestParam(required = false) String field,           // 분야 필터 (인문, 사회, 예술, 과학, 기술, 독서론)
            @RequestParam(required = false) String search,          // 검색어 (제목, 키워드 대상)
            @RequestParam(defaultValue = "date") String sort,       // 정렬 기준 (date, title, favorite)
            @RequestParam(defaultValue = "desc") String order,      // 정렬 순서 (asc, desc)
            @AuthenticationPrincipal Long memCode
    ) {
        try {
            // 타입 유효성 검사
            if (!isValidStorageType(type)) {
                return ResponseEntity.badRequest().body("유효하지 않은 저장소 타입입니다: " + type);
            }

            System.out.println("통합 Storage 조회 요청 - type: "+type+" page: "+page+" field: "+field+" search: "+search);
            log.info("통합 Storage 조회 요청 - type: {}, page: {}, field: {}, search: {}",
                    type, page, field, search);

            // 통합 서비스 메서드 호출
            PassageListWithPaginationResponseDto response = storageService
                    .getStorageListWithPagination(
                            memCode,
                            type,
                            page,
                            size,
                            field,
                            search,
                            sort,
                            order
                    );

            log.info("통합 Storage 조회 완료 - type: {}, 아이템 수: {}", type, response.getItems().size());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청 파라미터 - type: {}, error: {}", type, e.getMessage());
            return ResponseEntity.badRequest().body("잘못된 요청입니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Storage 조회 중 오류 발생 - type: {}, error: {}", type, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("저장소 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * Storage 타입 유효성 검사
     */
    private boolean isValidStorageType(String type) {
        return type != null && (
                "recent".equals(type) ||
                        "favorite".equals(type) ||
                        "deleted".equals(type)
        );
    }



    // 지문 삭제
    @PutMapping("/remove/each")
    public ResponseEntity<?> removePassage(@RequestBody PassageDeleteRequestDto requestDto) {
        if (requestDto.getPasCodeList() == null || requestDto.getPasCodeList().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("삭제할 대상이 없습니다.");
        }

        try {
            boolean result = passageService.deletePassage(requestDto);
            if (result) {
                return ResponseEntity.ok("삭제 완료");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버에서 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 작업명(지문 이름) 변경
    @PutMapping("/update/title")
    public ResponseEntity<?> updatePassageTitle(@RequestBody PassageUpdateTitleRequestDto requestDto) {
        if (requestDto.getPasCode() == null || requestDto.getTitle() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 값이 없습니다.");
        }

        try {
            boolean result = passageService.updatePassageTitle(requestDto);
            if (result) {
                return ResponseEntity.ok("지문 제목이 수정되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.OK).body("기존 제목과 동일합니다.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버에서 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 파일 추출 (type: pdf/word/txt)
    @GetMapping("/export/each/{pasCode}")
    public ResponseEntity<byte[]> generateFile(@PathVariable("pasCode") Long pasCode, @RequestParam("type") String type) {
        try {
            // pasCode 유효성 검사 추가
            if (pasCode == null || pasCode <= 0) {
                throw new IllegalArgumentException("유효하지 않은 pasCode입니다 : " + pasCode);
            }
            PassageWithQuestionsResponseDto responseDto = passageService.getPassageWithQuestions(pasCode);

            // 응답 데이터 유효성 검증
            if (responseDto == null) {
                throw new IllegalArgumentException("해당 pasCode에 대한 데이터를 찾을 수 없습니다 : " + pasCode);
            }

            String fileName = responseDto.getTitle().trim();
            byte[] result = generateFile(responseDto, type);

            HttpHeaders headers = createHeaders(fileName, type);

            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // 에러 로그 기록
            System.err.println("지문을 찾을 수 없음 : " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 에러 로그 기록
            System.err.println("파일 생성 오류 : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // 파일 생성
    private byte[] generateFile(PassageWithQuestionsResponseDto dto, String type) {
        return switch (type.toLowerCase()) {
            case "pdf" -> pdfService.createPdfFromDto(dto);
            case "word" -> wordService.createWordFromDto(dto);
            case "txt" -> txtService.createTxtFromDto(dto);
            default -> throw new IllegalArgumentException("Unsupported file type: " + type);
        };
    }

    // 파일 추출을 위한 httpheader 생성
    private HttpHeaders createHeaders(String fileName, String type) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();

        String extension;
        String contentType;

        switch (type.toLowerCase()) {
            case "pdf" -> {
                extension = "pdf";
                contentType = "application/pdf";
            }
            case "word" -> {
                extension = "docx";
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            case "txt" -> {
                extension = "txt";
                contentType = "text/plain; charset=UTF-8";
            }
            default -> throw new IllegalArgumentException("Unsupported file type: " + type);
        }

        // 파일 이름을 UTF-8로 URL 인코딩
        String encodedFileName = URLEncoder.encode(fileName + "." + extension, "UTF-8").replace("+", "%20"); // 공백을 `%20`으로 변환

        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        headers.add("Access-Control-Expose-Headers", "Content-Disposition");

        return headers;
    }
}
