package com.smartcampus.platform.answer.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.answer.dto.AnswerRequest;
import com.smartcampus.platform.answer.dto.AnswerResponse;
import com.smartcampus.platform.answer.entity.Answer;
import com.smartcampus.platform.answer.repository.AnswerRepository;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.doubt.entity.Doubt;
import com.smartcampus.platform.doubt.repository.DoubtRepository;

@Service
@Transactional
public class AnswerService {
  private final AnswerRepository answerRepository;
  private final DoubtRepository doubtRepository;
  private final UserRepository userRepository;

  public AnswerService(
      AnswerRepository answerRepository,
      DoubtRepository doubtRepository,
      UserRepository userRepository
  ) {
    this.answerRepository = answerRepository;
    this.doubtRepository = doubtRepository;
    this.userRepository = userRepository;
  }

  public AnswerResponse create(AnswerRequest request) {
    Doubt doubt = doubtRepository.findById(request.getDoubtId())
        .orElseThrow(() -> new ResourceNotFoundException("Doubt not found"));
    User author = userRepository.findById(request.getAuthorId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Answer answer = new Answer(doubt, author, request.getContent());
    return toResponse(answerRepository.save(answer));
  }

  public List<AnswerResponse> findAll() {
    return answerRepository.findAll().stream().map(this::toResponse).toList();
  }

  public AnswerResponse findById(Long id) {
    return toResponse(getAnswer(id));
  }

  public AnswerResponse update(Long id, AnswerRequest request) {
    Answer answer = getAnswer(id);
    Doubt doubt = doubtRepository.findById(request.getDoubtId())
        .orElseThrow(() -> new ResourceNotFoundException("Doubt not found"));
    User author = userRepository.findById(request.getAuthorId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    answer.setDoubt(doubt);
    answer.setAuthor(author);
    answer.setContent(request.getContent());

    return toResponse(answerRepository.save(answer));
  }

  public void delete(Long id) {
    Answer answer = getAnswer(id);
    answerRepository.delete(answer);
  }

  private Answer getAnswer(Long id) {
    return answerRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));
  }

  private AnswerResponse toResponse(Answer answer) {
    return new AnswerResponse(
        answer.getId(),
        answer.getDoubt().getId(),
        answer.getAuthor().getId(),
        answer.getAuthor().getFullName(),
        answer.getAuthor().getRole().name(),
        answer.getContent(),
        answer.getCreatedAt()
    );
  }
}
