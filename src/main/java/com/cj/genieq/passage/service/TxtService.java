package com.cj.genieq.passage.service;

import com.cj.genieq.passage.dto.response.PassageWithQuestionsResponseDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class TxtService {

    private String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        // HTML 태그 제거 정규식
        return html.replaceAll("<[^>]*>", "");
    }

    public byte[] createTxtFromDto(PassageWithQuestionsResponseDto dto) {
        StringBuilder sb = new StringBuilder();

        // ✅ 제목 작성
        sb.append("[작업 이름]").append("\n").append(dto.getTitle()).append("\n\n");

        // ✅ 본문 작성
        sb.append("[지문]").append("\n");
        sb.append(stripHtmlTags(dto.getContent())).append("\n\n");

        // ✅ 문제 작성
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            sb.append("[문항] ").append("\n");
            int num = 1;
            for (QuestionSelectResponseDto question : dto.getQuestions()) {
                sb.append(num).append(". ").append(stripHtmlTags(question.getQueQuery())).append("\n");

                String[] optionNums = {"①","②","③","④","⑤"};
                int optionNum = 0;
                // queOption 을 리스트에서 String Tiptap 방식으로 변경하게되면서 수정.
                for (String option : question.getQueOption().split("</p>")) {
                    String prefix = optionNums[optionNum];
                    sb.append(prefix).append(" ").append(stripHtmlTags(option)).append("\n");
                    optionNum++;
                }

                sb.append("정답: ").append(question.getQueAnswer()).append("\n");
                if (question.getQueDescription() != null && !question.getQueDescription().isEmpty()) {
                    sb.append("해설 : ").append(stripHtmlTags(question.getQueDescription())).append("\n");
                }

                sb.append("\n");
                num++;
            }
        }

        // ✅ 문자열을 바이트 배열로 변환 (UTF-8 인코딩)
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}