package com.smartcampus.platform.happenstance.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.happenstance.dto.HappenstanceAnalyticsResponse;
import com.smartcampus.platform.happenstance.dto.HappenstanceAdminAnalyticsResponse;
import com.smartcampus.platform.happenstance.dto.HappenstanceDomainStat;
import com.smartcampus.platform.happenstance.dto.HappenstanceOpportunityResponse;
import com.smartcampus.platform.happenstance.dto.HappenstanceOpportunityStat;
import com.smartcampus.platform.happenstance.dto.HappenstanceSaveResponse;
import com.smartcampus.platform.happenstance.dto.HappenstanceSerendipityScore;
import com.smartcampus.platform.happenstance.entity.HappenstanceClick;
import com.smartcampus.platform.happenstance.entity.HappenstanceOpportunity;
import com.smartcampus.platform.happenstance.entity.HappenstanceSave;
import com.smartcampus.platform.happenstance.repository.HappenstanceClickRepository;
import com.smartcampus.platform.happenstance.repository.HappenstanceOpportunityRepository;
import com.smartcampus.platform.happenstance.repository.HappenstanceSaveRepository;
import com.smartcampus.platform.mentormatching.repository.StudentPreferenceRepository;
import com.smartcampus.platform.student.repository.StudentRepository;

@Service
@Transactional
public class HappenstanceService {
  private final HappenstanceOpportunityRepository opportunityRepository;
  private final HappenstanceSaveRepository saveRepository;
  private final HappenstanceClickRepository clickRepository;
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final StudentPreferenceRepository preferenceRepository;

  public HappenstanceService(
      HappenstanceOpportunityRepository opportunityRepository,
      HappenstanceSaveRepository saveRepository,
      HappenstanceClickRepository clickRepository,
      UserRepository userRepository,
      StudentRepository studentRepository,
      StudentPreferenceRepository preferenceRepository
  ) {
    this.opportunityRepository = opportunityRepository;
    this.saveRepository = saveRepository;
    this.clickRepository = clickRepository;
    this.userRepository = userRepository;
    this.studentRepository = studentRepository;
    this.preferenceRepository = preferenceRepository;
  }

  public List<HappenstanceOpportunityResponse> getFeed(String email) {
    Long userId = resolveUserId(email);
    Set<Long> savedIds = saveRepository.findByUserId(userId)
        .stream()
        .map(save -> save.getOpportunity().getId())
        .collect(Collectors.toSet());

    return opportunityRepository.findAll()
        .stream()
        .sorted(Comparator.comparing(HappenstanceOpportunity::getCreatedAt).reversed())
        .map(item -> toResponse(item, savedIds.contains(item.getId())))
        .toList();
  }

  public HappenstanceSaveResponse toggleSave(String email, Long opportunityId) {
    Long userId = resolveUserId(email);
    HappenstanceOpportunity opportunity = opportunityRepository.findById(opportunityId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));

    var existing = saveRepository.findByUserIdAndOpportunityId(userId, opportunityId);
    boolean saved;
    if (existing.isPresent()) {
      saveRepository.delete(existing.get());
      saved = false;
    } else {
      saveRepository.save(new HappenstanceSave(userId, opportunity, LocalDateTime.now()));
      saved = true;
    }
    return new HappenstanceSaveResponse(opportunityId, saved);
  }

  public void recordClick(String email, Long opportunityId) {
    Long userId = resolveUserId(email);
    HappenstanceOpportunity opportunity = opportunityRepository.findById(opportunityId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
    clickRepository.save(new HappenstanceClick(userId, opportunity, LocalDateTime.now()));
  }

  public HappenstanceAnalyticsResponse getAnalytics(String email) {
    Long userId = resolveUserId(email);

    List<HappenstanceDomainStat> topClicked = clickRepository.countClicksByDomain()
        .stream()
        .map(row -> new HappenstanceDomainStat(
            String.valueOf(row[0]),
            ((Number) row[1]).longValue()
        ))
        .limit(6)
        .toList();

    List<HappenstanceDomainStat> mostSaved = saveRepository.countSavesByDomain()
        .stream()
        .map(row -> new HappenstanceDomainStat(
            String.valueOf(row[0]),
            ((Number) row[1]).longValue()
        ))
        .limit(6)
        .toList();

    List<HappenstanceOpportunityStat> mostSavedOpps = saveRepository.countSavesByOpportunity()
        .stream()
        .map(row -> new HappenstanceOpportunityStat(
            ((Number) row[0]).longValue(),
            String.valueOf(row[1]),
            ((Number) row[2]).longValue()
        ))
        .limit(5)
        .toList();

    long totalClicks = clickRepository.count();
    long totalSaves = saveRepository.count();

    HappenstanceSerendipityScore score = computeScore(userId);

    return new HappenstanceAnalyticsResponse(
        topClicked,
        mostSaved,
        mostSavedOpps,
        totalClicks,
        totalSaves,
        score
    );
  }

  public HappenstanceAdminAnalyticsResponse getAdminAnalytics(String email) {
    ensureAdmin(email);

    List<HappenstanceDomainStat> topClicked = clickRepository.countClicksByDomain()
        .stream()
        .map(row -> new HappenstanceDomainStat(
            String.valueOf(row[0]),
            ((Number) row[1]).longValue()
        ))
        .limit(6)
        .toList();

    List<HappenstanceDomainStat> mostSaved = saveRepository.countSavesByDomain()
        .stream()
        .map(row -> new HappenstanceDomainStat(
            String.valueOf(row[0]),
            ((Number) row[1]).longValue()
        ))
        .limit(6)
        .toList();

    List<HappenstanceOpportunityStat> mostSavedOpps = saveRepository.countSavesByOpportunity()
        .stream()
        .map(row -> new HappenstanceOpportunityStat(
            ((Number) row[0]).longValue(),
            String.valueOf(row[1]),
            ((Number) row[2]).longValue()
        ))
        .limit(5)
        .toList();

    long totalClicks = clickRepository.count();
    long totalSaves = saveRepository.count();

    java.util.Set<Long> activeUsers = new java.util.HashSet<>(clickRepository.findDistinctUserIds());
    activeUsers.addAll(saveRepository.findDistinctUserIds());

    double avgScore = 0;
    if (!activeUsers.isEmpty()) {
      int totalScore = 0;
      for (Long userId : activeUsers) {
        totalScore += computeScoreForUser(userId).getScore();
      }
      avgScore = totalScore / (double) activeUsers.size();
    }

    return new HappenstanceAdminAnalyticsResponse(
        topClicked,
        mostSaved,
        mostSavedOpps,
        totalClicks,
        totalSaves,
        activeUsers.size(),
        avgScore
    );
  }

  public List<String> getInterests(String email) {
    Long userId = resolveUserId(email);
    var student = studentRepository.findByUserId(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
    var preference = preferenceRepository.findByStudentId(student.getId()).orElse(null);
    if (preference == null) {
      return List.of();
    }

    String source = String.join(" ",
        preference.getRequiredSkills() == null ? "" : preference.getRequiredSkills(),
        preference.getLearningGoals() == null ? "" : preference.getLearningGoals()
    ).toLowerCase(Locale.ROOT);

    if (source.isBlank()) {
      return List.of();
    }

    Map<String, String> keywordMap = interestKeywords();
    java.util.Set<String> interests = new java.util.LinkedHashSet<>();
    for (var entry : keywordMap.entrySet()) {
      if (source.contains(entry.getKey())) {
        interests.add(entry.getValue());
      }
    }
    return new java.util.ArrayList<>(interests);
  }

  public List<HappenstanceOpportunityResponse> getRecommendations(String email) {
    Long userId = resolveUserId(email);
    Set<Long> savedIds = saveRepository.findByUserId(userId)
        .stream()
        .map(save -> save.getOpportunity().getId())
        .collect(Collectors.toSet());
    List<String> interests = getInterests(email);

    return opportunityRepository.findAll()
        .stream()
        .sorted((a, b) -> Integer.compare(scoreOpportunity(b, interests), scoreOpportunity(a, interests)))
        .map(item -> toResponse(item, savedIds.contains(item.getId())))
        .toList();
  }

  private HappenstanceOpportunityResponse toResponse(HappenstanceOpportunity item, boolean saved) {
    List<String> tags = parseTags(item.getTagsCsv());
    return new HappenstanceOpportunityResponse(
        item.getId(),
        item.getTitle(),
        item.getDescription(),
        item.getDomain(),
        item.getLink(),
        item.getPlatform(),
        item.getType(),
        tags,
        item.getDateLabel(),
        item.getLocation(),
        item.isTrending(),
        saved
    );
  }

  private List<String> parseTags(String tagsCsv) {
    if (tagsCsv == null || tagsCsv.isBlank()) {
      return List.of();
    }
    String[] parts = tagsCsv.split(",");
    List<String> tags = new ArrayList<>();
    for (String part : parts) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        tags.add(trimmed);
      }
    }
    return tags;
  }

  private HappenstanceSerendipityScore computeScore(Long userId) {
    return computeScoreForUser(userId);
  }

  private HappenstanceSerendipityScore computeScoreForUser(Long userId) {
    List<HappenstanceClick> clicks = clickRepository.findByUserId(userId);
    List<HappenstanceSave> saves = saveRepository.findByUserId(userId);

    Map<String, Integer> domainCounts = new HashMap<>();
    int total = 0;

    for (HappenstanceClick click : clicks) {
      String domain = normalize(click.getOpportunity().getDomain());
      if (domain != null) {
        domainCounts.merge(domain, 1, Integer::sum);
        total++;
      }
    }

    for (HappenstanceSave save : saves) {
      String domain = normalize(save.getOpportunity().getDomain());
      if (domain != null) {
        domainCounts.merge(domain, 1, Integer::sum);
        total++;
      }
    }

    if (total == 0) {
      return new HappenstanceSerendipityScore(0, 0, 0, 0);
    }

    int unique = domainCounts.size();
    int max = domainCounts.values().stream().max(Integer::compareTo).orElse(0);
    int outOfComfort = Math.max(0, total - max);
    int score = Math.min(100, unique * 12 + outOfComfort * 4);

    return new HappenstanceSerendipityScore(score, unique, total, outOfComfort);
  }

  private Long resolveUserId(String email) {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    return user.getId();
  }

  private void ensureAdmin(String email) {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }
  }

  private String normalize(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
  }

  private Map<String, String> interestKeywords() {
    Map<String, String> map = new java.util.LinkedHashMap<>();
    map.put("ai", "AI");
    map.put("machine learning", "AI");
    map.put("ml", "AI");
    map.put("data", "Data Science");
    map.put("analytics", "Data Science");
    map.put("web", "Web Dev");
    map.put("frontend", "Web Dev");
    map.put("backend", "Web Dev");
    map.put("full stack", "Web Dev");
    map.put("startup", "Startup");
    map.put("entrepreneur", "Startup");
    map.put("design", "Design");
    map.put("ui", "Design");
    map.put("ux", "Design");
    map.put("product", "Product");
    map.put("cyber", "Cybersecurity");
    map.put("security", "Cybersecurity");
    map.put("iot", "IoT");
    map.put("hardware", "IoT");
    map.put("mobile", "Mobile");
    map.put("android", "Mobile");
    map.put("ios", "Mobile");
    map.put("open source", "Open Source");
    return map;
  }

  private int scoreOpportunity(HappenstanceOpportunity opportunity, List<String> interests) {
    if (interests == null || interests.isEmpty()) {
      return opportunity.isTrending() ? 2 : 0;
    }
    int score = 0;
    String domain = normalize(opportunity.getDomain());
    if (domain != null && interests.stream().anyMatch(interest -> interest.equalsIgnoreCase(domain))) {
      score += 6;
    }
    for (String tag : parseTags(opportunity.getTagsCsv())) {
      if (interests.stream().anyMatch(interest -> interest.equalsIgnoreCase(tag))) {
        score += 2;
      }
    }
    if (opportunity.isTrending()) {
      score += 1;
    }
    return score;
  }
}
