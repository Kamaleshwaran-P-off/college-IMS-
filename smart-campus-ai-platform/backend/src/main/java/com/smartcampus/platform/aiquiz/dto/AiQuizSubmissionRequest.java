package com.smartcampus.platform.aiquiz.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class AiQuizSubmissionRequest {
  @NotNull
  private Long quizId;

  @NotNull
  private List<AiQuizAnswer> answers;

  private Integer timeTakenSeconds;

  public Long getQuizId() {
    return quizId;
  }

  public void setQuizId(Long quizId) {
    this.quizId = quizId;
  }

  public List<AiQuizAnswer> getAnswers() {
    return answers;
  }

  public void setAnswers(List<AiQuizAnswer> answers) {
    this.answers = answers;
  }

  public Integer getTimeTakenSeconds() {
    return timeTakenSeconds;
  }

  public void setTimeTakenSeconds(Integer timeTakenSeconds) {
    this.timeTakenSeconds = timeTakenSeconds;
  }

  public static class AiQuizAnswer {
    private int index;
    private String answer;

    public AiQuizAnswer() {}

    public AiQuizAnswer(int index, String answer) {
      this.index = index;
      this.answer = answer;
    }

    public int getIndex() {
      return index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public String getAnswer() {
      return answer;
    }

    public void setAnswer(String answer) {
      this.answer = answer;
    }
  }
}
