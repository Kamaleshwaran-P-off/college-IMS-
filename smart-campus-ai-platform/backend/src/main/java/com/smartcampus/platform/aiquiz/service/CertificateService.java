package com.smartcampus.platform.aiquiz.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class CertificateService {
  private static final String TEMPLATE_PATH = "certificates/template.png";

  public byte[] generateCertificate(
      String studentName,
      String registerNumber,
      String quizTitle,
      String department,
      String section,
      int score,
      int total,
      Integer timeTakenSeconds,
      Integer durationMinutes
  ) throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      try (PDPageContentStream content = new PDPageContentStream(document, page)) {
        float width = page.getMediaBox().getWidth();
        float height = page.getMediaBox().getHeight();

        drawTemplate(document, content, width, height);

        content.setNonStrokingColor(20, 23, 38);
        drawText(content, PDType1Font.HELVETICA_BOLD, 26, 150, height - 160, "Certificate of Achievement");

        content.setNonStrokingColor(55, 65, 81);
        drawText(content, PDType1Font.HELVETICA, 13, 150, height - 205, "This is to certify that");

        String identity = studentName + (registerNumber != null && !registerNumber.isBlank() ? " (" + registerNumber + ")" : "");
        if (department != null && section != null) {
          identity += " - " + department + " " + section;
        }
        content.setNonStrokingColor(17, 24, 39);
        drawText(content, PDType1Font.HELVETICA_BOLD, 18, 150, height - 235, identity);

        content.setNonStrokingColor(55, 65, 81);
        drawText(content, PDType1Font.HELVETICA, 13, 150, height - 265, "has successfully completed the quiz:");

        content.setNonStrokingColor(17, 24, 39);
        drawText(content, PDType1Font.HELVETICA_BOLD, 16, 150, height - 290, quizTitle != null ? quizTitle : "AI Quiz");

        content.setNonStrokingColor(31, 41, 55);
        drawText(content, PDType1Font.HELVETICA, 13, 150, height - 320, "Score: " + score + " / " + total);
        if (timeTakenSeconds != null) {
          int minutes = timeTakenSeconds / 60;
          int seconds = timeTakenSeconds % 60;
          String durationLabel = String.format("%02d:%02d", minutes, seconds);
          drawText(content, PDType1Font.HELVETICA, 13, 150, height - 345,
              "Time Taken: " + durationLabel + (durationMinutes != null ? " / " + durationMinutes + " min" : ""));
        }

        content.setNonStrokingColor(75, 85, 99);
        drawText(content, PDType1Font.HELVETICA_OBLIQUE, 12, 150, height - 375,
            "Issued on: " + LocalDate.now());

        drawText(content, PDType1Font.HELVETICA_OBLIQUE, 12, width - 240, 90,
            "Smart Campus AI Platform");
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      document.save(out);
      return out.toByteArray();
    }
  }

  private void drawTemplate(PDDocument document, PDPageContentStream content, float pageWidth, float pageHeight) throws IOException {
    ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
    if (!resource.exists()) {
      return;
    }
    byte[] imageBytes = resource.getContentAsByteArray();
    PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, "certificate-template");
    float imageWidth = image.getWidth();
    float imageHeight = image.getHeight();
    float scale = Math.min(pageWidth / imageWidth, pageHeight / imageHeight);
    float drawWidth = imageWidth * scale;
    float drawHeight = imageHeight * scale;
    float x = (pageWidth - drawWidth) / 2f;
    float y = (pageHeight - drawHeight) / 2f;
    content.drawImage(image, x, y, drawWidth, drawHeight);
  }

  private void drawText(PDPageContentStream content, PDType1Font font, int size, float x, float y, String text) throws IOException {
    if (text == null || text.isBlank()) {
      return;
    }
    content.setFont(font, size);
    content.beginText();
    content.newLineAtOffset(x, y);
    content.showText(text);
    content.endText();
  }
}
