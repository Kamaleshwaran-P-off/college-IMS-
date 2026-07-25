package com.smartcampus.platform.answer.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.doubt.entity.Doubt;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "answers")
public class Answer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "doubt_id", nullable = false)
  private Doubt doubt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @NotBlank
  @Lob
  private String content;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Answer() {}

  public Answer(Doubt doubt, User author, String content) {
    this.doubt = doubt;
    this.author = author;
    this.content = content;
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

  public Doubt getDoubt() {
    return doubt;
  }

  public void setDoubt(Doubt doubt) {
    this.doubt = doubt;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
