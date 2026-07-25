package com.smartcampus.platform.queryusage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.queryusage.dto.QueryUsageRequest;
import com.smartcampus.platform.queryusage.dto.QueryUsageResponse;
import com.smartcampus.platform.queryusage.entity.QueryUsage;
import com.smartcampus.platform.queryusage.repository.QueryUsageRepository;

@Service
@Transactional
public class QueryUsageService {
  private final QueryUsageRepository queryUsageRepository;
  private final UserRepository userRepository;

  public QueryUsageService(QueryUsageRepository queryUsageRepository, UserRepository userRepository) {
    this.queryUsageRepository = queryUsageRepository;
    this.userRepository = userRepository;
  }

  public QueryUsageResponse create(QueryUsageRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    QueryUsage usage = new QueryUsage(
        user,
        request.getQueryType(),
        request.getTokensUsed(),
        request.getCost()
    );

    return toResponse(queryUsageRepository.save(usage));
  }

  public List<QueryUsageResponse> findAll() {
    return queryUsageRepository.findAll().stream().map(this::toResponse).toList();
  }

  public QueryUsageResponse findById(Long id) {
    return toResponse(getUsage(id));
  }

  public QueryUsageResponse update(Long id, QueryUsageRequest request) {
    QueryUsage usage = getUsage(id);
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    usage.setUser(user);
    usage.setQueryType(request.getQueryType());
    usage.setTokensUsed(request.getTokensUsed());
    usage.setCost(request.getCost());

    return toResponse(queryUsageRepository.save(usage));
  }

  public void delete(Long id) {
    QueryUsage usage = getUsage(id);
    queryUsageRepository.delete(usage);
  }

  private QueryUsage getUsage(Long id) {
    return queryUsageRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Query usage record not found"));
  }

  private QueryUsageResponse toResponse(QueryUsage usage) {
    return new QueryUsageResponse(
        usage.getId(),
        usage.getUser().getId(),
        usage.getQueryType(),
        usage.getTokensUsed(),
        usage.getCost(),
        usage.getCreatedAt()
    );
  }
}
