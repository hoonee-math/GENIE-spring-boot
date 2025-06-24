package com.cj.genieq.passage.service;

import com.cj.genieq.passage.dto.response.PassageWithQuestionsResponseDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import org.apache.poi.xwpf.usermodel.*;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

@Service
public class WordService {

    private String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        // HTML 태그 제거 정규식
        return html.replaceAll("<[^>]*>", "");
    }

    public byte[] createWordFromDto(PassageWithQuestionsResponseDto dto) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // ✅ Word 문서 생성
            XWPFDocument document = new XWPFDocument();

            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("다음 글을 읽고 물음에 답하시오.");
            titleRun.setBold(true);
            titleRun.setFontSize(10);

            // ✅ 본문 작성
            XWPFTable contentTable = document.createTable(1, 1);

            // 테이블 테두리 설정
            contentTable.setWidth("100%");

            // 테이블의 테두리 설정
            XWPFTableCell cell = contentTable.getRow(0).getCell(0);
            CTTcBorders borders = cell.getCTTc().addNewTcPr().addNewTcBorders();

            // 상단 테두리
            CTBorder topBorder = borders.addNewTop();
            topBorder.setVal(STBorder.SINGLE);
            topBorder.setSz(BigInteger.valueOf(4));
            topBorder.setColor("000000");

            // 하단 테두리
            CTBorder bottomBorder = borders.addNewBottom();
            bottomBorder.setVal(STBorder.SINGLE);
            bottomBorder.setSz(BigInteger.valueOf(4));
            bottomBorder.setColor("000000");

            // 좌측 테두리
            CTBorder leftBorder = borders.addNewLeft();
            leftBorder.setVal(STBorder.SINGLE);
            leftBorder.setSz(BigInteger.valueOf(4));
            leftBorder.setColor("000000");

            // 우측 테두리
            CTBorder rightBorder = borders.addNewRight();
            rightBorder.setVal(STBorder.SINGLE);
            rightBorder.setSz(BigInteger.valueOf(4));
            rightBorder.setColor("000000");

            // 셀 너비 설정
            cell.setWidth("100%");

            // 셀 안에 본문 텍스트 추가
            XWPFParagraph contentParagraph = cell.getParagraphs().get(0);
            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setText(stripHtmlTags(dto.getContent()));
            contentRun.setFontSize(10);

            // 테이블 후 간격 추가
            XWPFParagraph spacingParagraph = document.createParagraph();
            spacingParagraph.setSpacingAfter(200);

            // ✅ 문제 작성
            if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
                int questionNum = 1;
                for (QuestionSelectResponseDto question : dto.getQuestions()) {
                    // ✅ 문제 출력 (Bold)
                    XWPFParagraph questionParagraph = document.createParagraph();
                    XWPFRun questionRun = questionParagraph.createRun();
                    questionRun.setText(questionNum + ". " + stripHtmlTags(question.getQueQuery()));
                    questionRun.setBold(true);
                    questionRun.setFontSize(10);

                    // ✅ 선택지 출력
                    String[] optionNums = {"①","②","③","④","⑤"};
                    int optionNum = 0;
                    for (String option : question.getQueOption()) {
                        XWPFParagraph optionParagraph = document.createParagraph();
                        optionParagraph.setIndentationLeft(500); // 들여쓰기
                        XWPFRun optionRun = optionParagraph.createRun();
                        String prefix = optionNums[optionNum];
                        optionRun.setText(prefix + " " + stripHtmlTags(option));
                        optionRun.setFontSize(10);
                        optionNum++;
                    }

                    // 테이블 후 간격 추가
                    spacingParagraph = document.createParagraph();
                    spacingParagraph.setSpacingAfter(200);

                    // ✅ 정답 출력
                    XWPFParagraph answerParagraph = document.createParagraph();
                    XWPFRun answerRun = answerParagraph.createRun();
                    answerRun.setText("정답: " + question.getQueAnswer());
                    answerRun.setBold(true);
                    answerRun.setFontSize(10);

                    // 해설 출력 (있는 경우)
                    if (question.getDescription() != null && !question.getDescription().isEmpty()) {
                        XWPFParagraph descriptionParagraph = document.createParagraph();
                        XWPFRun descriptionRun = descriptionParagraph.createRun();
                        descriptionRun.setText("해설 : " + stripHtmlTags(question.getDescription()));
                        descriptionRun.setFontSize(10);
                    }

                    // 문제 사이 간격
                    XWPFParagraph spaceParagraph = document.createParagraph();
                    spaceParagraph.setSpacingAfter(100);

                    questionNum++;
                }
            }

            // ✅ 문서 작성 후 닫기
            document.write(baos);
            document.close();

            return baos.toByteArray(); // Word 데이터를 바이트 배열로 반환
        } catch (Exception e) {
            throw new RuntimeException("Word 파일 생성 실패", e);
        }
    }
}

