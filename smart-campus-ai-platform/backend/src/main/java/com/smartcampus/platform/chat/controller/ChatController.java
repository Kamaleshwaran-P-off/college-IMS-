package com.smartcampus.platform.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.chat.dto.ChatRequest;
import com.smartcampus.platform.chat.dto.ChatResponse;
import com.smartcampus.platform.chat.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@Validated
public class ChatController {
  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  @PostMapping
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(chatService.chat(request));
  }
}
