package com.smartcampus.platform.chat.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_bonus")
public class ChatBonus {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private int bonusQueries;

  @Column(length = 50, nullable = false)
  private String source;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public ChatBonus() {}

  public ChatBonus(User user, int bonusQueries, String source) {
    this.user = user;
    this.bonusQueries = bonusQueries;
    this.source = source;
  }

  @PrePersist
  public void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public int getBonusQueries() {
    return bonusQueries;
  }

  public String getSource() {
    return source;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
