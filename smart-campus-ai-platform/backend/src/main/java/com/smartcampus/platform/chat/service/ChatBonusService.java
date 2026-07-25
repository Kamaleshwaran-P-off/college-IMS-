package com.smartcampus.platform.chat.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.chat.entity.ChatBonus;
import com.smartcampus.platform.chat.repository.ChatBonusRepository;

@Service
@Transactional
public class ChatBonusService {
  private final ChatBonusRepository chatBonusRepository;

  public ChatBonusService(ChatBonusRepository chatBonusRepository) {
    this.chatBonusRepository = chatBonusRepository;
  }

  public void grantBonus(User user, int bonusQueries, String source) {
    if (bonusQueries <= 0) {
      return;
    }
    chatBonusRepository.save(new ChatBonus(user, bonusQueries, source));
  }

  public int getBonusForToday(Long userId) {
    LocalDate today = LocalDate.now();
    LocalDateTime start = today.atStartOfDay();
    LocalDateTime end = today.plusDays(1).atStartOfDay();
    return chatBonusRepository.sumBonusQueries(userId, start, end);
  }
}
