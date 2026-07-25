package com.smartcampus.platform.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.ai.dto.AiExplainRequest;
import com.smartcampus.platform.ai.dto.AiGenerateQuizRequest;
import com.smartcampus.platform.ai.dto.AiSummarizeRequest;
import com.smartcampus.platform.ai.dto.AiTextResponse;
import com.smartcampus.platform.ai.service.AiAssistantService;

@RestController
@RequestMapping("/api/ai")
public class AiController {
  private final AiAssistantService aiAssistantService;

  public AiController(AiAssistantService aiAssistantService) {
    this.aiAssistantService = aiAssistantService;
  }

  @PostMapping("/explain")
  public ResponseEntity<AiTextResponse> explain(@RequestBody AiExplainRequest request) {
    String result = aiAssistantService.explainTopic(request);
    return ResponseEntity.ok(new AiTextResponse(result));
  }

  @PostMapping("/summarize")
  public ResponseEntity<AiTextResponse> summarize(@RequestBody AiSummarizeRequest request) {
    String result = aiAssistantService.summarizeContent(request);
    return ResponseEntity.ok(new AiTextResponse(result));
  }

  @PostMapping("/generate-quiz")
  public ResponseEntity<AiTextResponse> generateQuiz(@RequestBody AiGenerateQuizRequest request) {
    String result = aiAssistantService.generateQuiz(request);
    return ResponseEntity.ok(new AiTextResponse(result));
  }
}
