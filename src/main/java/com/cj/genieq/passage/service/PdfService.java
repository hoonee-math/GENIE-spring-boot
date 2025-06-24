package com.cj.genieq.passage.service;

import com.cj.genieq.passage.dto.response.PassageWithQuestionsResponseDto;
import com.cj.genieq.question.dto.response.QuestionSelectResponseDto;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfService {

    private static final float MARGIN = 80;
    private static final float CONTENT_WIDTH = PDRectangle.A4.getWidth() - (2 * MARGIN);
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    private String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        // HTML 태그 제거 정규식
        return html.replaceAll("<[^>]*>", "");
    }

    // 폰트 관련 필드 추가
    private PDType0Font regularFont;
    private PDType0Font boldFont;

    // 폰트 로드 메서드
    private void loadFonts(PDDocument document) throws IOException {
        regularFont = PDType0Font.load(document, new ClassPathResource("fonts/BookkMyungjo_Light.ttf").getInputStream());
        boldFont = PDType0Font.load(document, new ClassPathResource("fonts/BookkMyungjo_Bold.ttf").getInputStream());
    }

    // 개행 문자 처리를 위한 renderFormattedText 메서드 수정
    private float renderFormattedText(PDDocument document, PDPageContentStream contentStream,
                                      String html, float x, float y, int fontSize, float width, float boxStartX, float boxStartY) throws IOException {
        if (html == null || html.isEmpty()) {
            return y;
        }

        // 개행 문자('\n')를 <br> 태그로 변환
        html = html.replace("\n", "<br>");

        float currentX = x;
        float currentY = y;
        float startY = y;
        float height = 0;
        float lineHeight = fontSize * 1.5f;
        float currentWidth = 0;
        StringBuilder currentText = new StringBuilder();

        // 현재 스타일 상태
        boolean isBold = false;
        boolean isUnderline = false;
        boolean isStrikethrough = false;

        int i = 0;
        while (i < html.length()) {
            if (html.charAt(i) == '<') {
                // 현재까지 수집된 텍스트 출력
                if (currentText.length() > 0) {
                    drawStyledText(contentStream, currentText.toString(), currentX, currentY, fontSize,
                            isBold, isUnderline, isStrikethrough);

                    // X 위치 업데이트
                    float textWidth = (isBold ? boldFont : regularFont).getStringWidth(currentText.toString()) / 1000 * fontSize;
                    currentX += textWidth;
                    currentWidth += textWidth;
                    currentText.setLength(0); // 텍스트 버퍼 초기화
                }

                // 태그 찾기
                int closeTagIndex = html.indexOf('>', i);
                if (closeTagIndex == -1) break;

                String tag = html.substring(i, closeTagIndex + 1).toLowerCase();

                // 스타일 태그 처리
                if (tag.equals("<b>")) {
                    isBold = true;
                } else if (tag.equals("</b>")) {
                    isBold = false;
                } else if (tag.equals("<u>")) {
                    isUnderline = true;
                } else if (tag.equals("</u>")) {
                    isUnderline = false;
                } else if (tag.equals("<s>") || tag.equals("<del>")) {
                    isStrikethrough = true;
                } else if (tag.equals("</s>") || tag.equals("</del>")) {
                    isStrikethrough = false;
                } else if (tag.equals("<br>")) {
                    // 줄바꿈 처리
                    currentY -= lineHeight;
                    height += lineHeight;
                    currentX = x;
                    currentWidth = 0;
                }

                i = closeTagIndex + 1;
            } else {
                // 단어 단위 처리
                int spaceIndex = html.indexOf(' ', i);
                int tagIndex = html.indexOf('<', i);
                int endIndex;

                if (spaceIndex == -1) spaceIndex = html.length();
                if (tagIndex == -1) tagIndex = html.length();
                endIndex = Math.min(spaceIndex, tagIndex);

                // 현재 위치부터 공백 또는 태그까지의 단어를 추출
                String word = html.substring(i, endIndex);

                // 단어 너비 계산
                float wordWidth = (isBold ? boldFont : regularFont).getStringWidth(word + (spaceIndex == endIndex ? " " : "")) / 1000 * fontSize;

                // 줄바꿈 처리
                if (currentWidth + wordWidth > width && currentWidth > 0) {
                    // 현재까지 수집된 텍스트 출력
                    if (currentText.length() > 0) {
                        drawStyledText(contentStream, currentText.toString(), currentX, currentY, fontSize,
                                isBold, isUnderline, isStrikethrough);
                        currentText.setLength(0);
                    }

                    // 줄바꿈
                    currentY -= lineHeight;
                    currentX = x;
                    currentWidth = 0;
                }

                // 단어 추가
                currentText.append(word);
                if (spaceIndex == endIndex && endIndex < html.length()) {
                    currentText.append(" ");
                }

                // 단어 너비 추가
                currentWidth += wordWidth;
                i = endIndex;
                if (spaceIndex == endIndex) i++; // 공백 건너뛰기
            }
        }

        // 남은 텍스트 출력
        if (currentText.length() > 0) {
            drawStyledText(contentStream, currentText.toString(), currentX, currentY, fontSize,
                    isBold, isUnderline, isStrikethrough);
        }

        // 지문이 있는 경우에만 테두리 그리기
        if (boxStartX > 0 && boxStartY > 0) {
            float boxHeight = startY - currentY + lineHeight;
            contentStream.setLineWidth(0.5f);
            contentStream.addRect(boxStartX - 10, currentY - lineHeight, width + 20, boxHeight + 20);
            contentStream.stroke();
        }

        return currentY - lineHeight;
    }

    // 스타일이 적용된 텍스트 그리기 메서드 수정
    private void drawStyledText(PDPageContentStream contentStream, String text,
                                float x, float y, int fontSize, boolean bold, boolean underline, boolean strikethrough) throws IOException {
        try {
            PDType0Font fontToUse = bold ? boldFont : regularFont;

            contentStream.beginText();
            contentStream.setFont(fontToUse, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();

            float textWidth = fontToUse.getStringWidth(text) / 1000 * fontSize;

            // 밑줄 그리기
            if (underline) {
                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(x, y - 1.5f);
                contentStream.lineTo(x + textWidth, y - 1.5f);
                contentStream.stroke();
            }

            // 취소선 그리기
            if (strikethrough) {
                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(x, y + fontSize/3);
                contentStream.lineTo(x + textWidth, y + fontSize/3);
                contentStream.stroke();
            }
        } catch (IOException e) {
            throw e;
        }
    }

    public byte[] createPdfFromDto(PassageWithQuestionsResponseDto dto) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 폰트 로드
            loadFonts(document);

            // 첫 페이지 생성
            PDPage contentPage = new PDPage(PDRectangle.A4);
            document.addPage(contentPage);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, contentPage)) {
                float currentX = MARGIN;
                float currentY = PAGE_HEIGHT - MARGIN;
                // 지문 전, 텍스트 출력
                contentStream.beginText();
                contentStream.setFont(regularFont, 10);
                contentStream.newLineAtOffset(currentX, currentY);
                contentStream.showText("다음 글을 읽고, 물음에 답하시오.");
                contentStream.endText();

                currentY -= 20;

                // 지문 테두리 그리기를 위한 시작점
                float boxStartX = currentX;
                float boxStartY = currentY;

                currentY -= 10;

                // 지문 출력 (여러 줄 처리)
                String content = dto.getContent();
                renderFormattedText(document, contentStream, content, currentX, currentY,
                        10, CONTENT_WIDTH - 20, boxStartX, boxStartY);
            }

            // 문항이 있는 경우, 새 페이지에 문항 출력
            if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
                PDPage questionPage = new PDPage(PDRectangle.A4);
                document.addPage(questionPage);

                try (PDPageContentStream questionStream = new PDPageContentStream(document, questionPage)) {
                    float yPosition = PAGE_HEIGHT - MARGIN;
                    int questionNum = 1;

                    for (QuestionSelectResponseDto question : dto.getQuestions()) {
                        // 문제 내용
                        String questionText = stripHtmlTags(question.getQueQuery());
                        questionStream.beginText();
                        questionStream.setFont(regularFont, 10);
                        questionStream.newLineAtOffset(50, yPosition);
                        questionStream.showText(questionNum + ". " + questionText);
                        questionStream.endText();

                        // 보기 출력
                        float optionY = yPosition - 30;
                        String[] optionNums = {"①","②","③","④","⑤"};
                        int optionNum = 0;
                        for (String option : question.getQueOption()) {
                            String prefix = (optionNum < optionNums.length) ? optionNums[optionNum] : String.valueOf(optionNum + 1) + ". ";

                            questionStream.beginText();
                            questionStream.setFont(regularFont, 10);
                            questionStream.newLineAtOffset(70, optionY);
                            questionStream.showText(prefix + " ");
                            questionStream.endText();

                            String optionText = stripHtmlTags(option);

                            float prefixWidth = regularFont.getStringWidth(prefix + " ") / 1000 * 10;
                            float newX = MARGIN + prefixWidth;
                            optionY = writeMultiLineText(document, questionStream, regularFont, optionText, newX, optionY,
                                    10, CONTENT_WIDTH - 20, 0, 0);

                            optionY -= 5;
                            optionNum++;
                        }
                        yPosition = optionY - 20;
                        questionNum++;
                    }
                }
            }

            // 새 페이지에 정답 및 해설 출력
            if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
                PDPage answerPage = new PDPage(PDRectangle.A4);
                document.addPage(answerPage);

                try (PDPageContentStream answerStream = new PDPageContentStream(document, answerPage)) {
                    float yPosition = PAGE_HEIGHT - MARGIN;
                    int questionNum = 1;

                    for (QuestionSelectResponseDto question : dto.getQuestions()) {
                        // 문제 번호 출력
                        answerStream.beginText();
                        answerStream.setFont(regularFont, 10);
                        answerStream.newLineAtOffset(MARGIN, yPosition);
                        answerStream.showText(questionNum + ". ");
                        answerStream.endText();

                        // 정답 출력
                        String answerText = question.getQueAnswer();
                        String circleText = "";
                        String[] optionNums = {"①","②","③","④","⑤"};

                        try {
                            int answerNum = Integer.parseInt(answerText);
                            if (answerNum >= 1 && answerNum <= 5) {
                                circleText = optionNums[answerNum - 1]; // 1부터 시작하는 번호를 0-4 인덱스로 변환
                            } else {
                                circleText = answerText; // 범위 밖의 번호는 그대로 출력
                            }
                        } catch (NumberFormatException e) {
                            // 숫자가 아닌 경우 원본 텍스트 사용
                            circleText = answerText;
                        }

                        answerStream.beginText();
                        answerStream.setFont(regularFont, 10);
                        answerStream.newLineAtOffset(MARGIN + 20, yPosition);
                        answerStream.showText("정답 : " + circleText);
                        answerStream.endText();

                        yPosition -= 20;

                        // 해설 출력
                        if (question.getDescription() != null && !question.getDescription().isEmpty()) {
                            answerStream.beginText();
                            answerStream.setFont(regularFont, 10);
                            answerStream.newLineAtOffset(MARGIN + 20, yPosition);
                            answerStream.showText("해설 : ");
                            answerStream.endText();

                            String description = stripHtmlTags(question.getDescription());

                            float labelWidth = regularFont.getStringWidth("해설 : ") / 1000 * 10;
                            float newX = MARGIN + 20 + labelWidth;

                            yPosition = writeMultiLineText(document, answerStream, regularFont, description,
                                    newX, yPosition, 10, CONTENT_WIDTH - 40, 0, 0);
                        } else {
                            yPosition -= 10;
                        }

                        yPosition -= 20; // 문제 간의 여백
                        questionNum++;
                    }
                }
            }
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("PDF 생성 실패 원인 : " + e.getMessage());
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    private float writeMultiLineText(PDDocument document, PDPageContentStream contentStream, PDType0Font font,
                                    String text, float x, float y, int fontSize, float width, float boxStartX, float boxStartY) throws IOException {

        String[] paragraphs = text.split("\n");
        float currentY = y;
        float startY = y;
        float height = 0;

        for (String paragraph : paragraphs) {
            // 단락 내에서 줄바꿈 처리 로직
            int lastSpace = -1;
            float startX = x;
            float currentWidth = 0;

            for (int i = 0; i < paragraph.length(); i++) {
                char c = paragraph.charAt(i);
                float charWidth = font.getStringWidth(String.valueOf(c)) / 1000 * fontSize;

                if (c == ' ') {
                    lastSpace = i;
                }

                if (currentWidth + charWidth > width) {
                    if (lastSpace >= 0) {
                        contentStream.beginText();
                        contentStream.setFont(font, fontSize);
                        contentStream.newLineAtOffset(startX, currentY);
                        contentStream.showText(paragraph.substring(0, lastSpace));
                        contentStream.endText();

                        paragraph = paragraph.substring(lastSpace + 1);
                        i = 0;
                        lastSpace = -1;
                        currentY -= fontSize * 1.5;
                        height += fontSize * 1.5;
                        currentWidth = 0;
                    }
                }
                currentWidth += charWidth;
            }
            // 남은 텍스트 출력
            if (paragraph.length() > 0) {
                try {
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    contentStream.newLineAtOffset(startX, currentY);
                    contentStream.showText(paragraph);
                } finally {
                    contentStream.endText(); // finally 블록에서 항상 호출되도록 함
                }
                currentY -= fontSize * 1.5;
                height += fontSize * 1.5;
            }
        }

        // 지문이 있는 경우에만 테두리 그리기
        if (boxStartX > 0 && boxStartY > 0) {
            // 박스의 높이는 시작 Y와 현재 Y의 차이로 계산
            float boxHeight = startY - currentY;
            contentStream.setLineWidth(0.5f);
            contentStream.addRect(boxStartX - 10, currentY, width + 20, boxHeight + 20);
            contentStream.stroke();
        }

        // 현재 Y위치 반환
        return currentY;
    }

    private float findLastYPosition(PDPageContentStream contentStream) {
        return 400;
    }
}