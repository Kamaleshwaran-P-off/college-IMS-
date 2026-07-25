package com.smartcampus.platform.feed.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.feed.dto.CareerFeedCreateRequest;
import com.smartcampus.platform.feed.dto.CareerFeedItemResponse;
import com.smartcampus.platform.feed.entity.CareerCategory;
import com.smartcampus.platform.feed.entity.CareerFeedItem;
import com.smartcampus.platform.feed.entity.CareerFeedSave;
import com.smartcampus.platform.feed.repository.CareerFeedItemRepository;
import com.smartcampus.platform.feed.repository.CareerFeedSaveRepository;

@RestController
@RequestMapping("/api/career-feed")
public class CareerFeedController {
  private final CareerFeedItemRepository itemRepository;
  private final CareerFeedSaveRepository saveRepository;
  private final UserRepository userRepository;

  public CareerFeedController(
      CareerFeedItemRepository itemRepository,
      CareerFeedSaveRepository saveRepository,
      UserRepository userRepository
  ) {
    this.itemRepository = itemRepository;
    this.saveRepository = saveRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/items")
  public List<CareerFeedItemResponse> getItems(Authentication authentication) {
    Long userId = getUserId(authentication);
    Set<Long> saved = saveRepository.findByUserId(userId)
        .stream()
        .map(save -> save.getItem().getId())
        .collect(java.util.stream.Collectors.toSet());

    return itemRepository.findAll()
        .stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .map(item -> mapItem(item, saved.contains(item.getId())))
        .toList();
  }

  @GetMapping("/saved")
  public List<CareerFeedItemResponse> getSaved(Authentication authentication) {
    Long userId = getUserId(authentication);
    return saveRepository.findByUserId(userId)
        .stream()
        .map(save -> mapItem(save.getItem(), true))
        .toList();
  }

  @PostMapping("/items")
  public ResponseEntity<CareerFeedItemResponse> createItem(
      Authentication authentication,
      @RequestBody CareerFeedCreateRequest request
  ) {
    ensureCreator(authentication);
    CareerCategory category = parseCategory(request.getCategory());

    CareerFeedItem item = new CareerFeedItem(
        request.getTitle(),
        request.getCreator(),
        request.getDescription(),
        category,
        request.getSourceUrl(),
        request.getThumbnailUrl(),
        LocalDateTime.now()
    );

    CareerFeedItem saved = itemRepository.save(item);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapItem(saved, false));
  }

  @PostMapping("/items/{id}/save")
  public ResponseEntity<CareerFeedItemResponse> toggleSave(Authentication authentication, @PathVariable Long id) {
    Long userId = getUserId(authentication);
    CareerFeedItem item = itemRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

    var existing = saveRepository.findByUserIdAndItemId(userId, id);
    boolean saved;
    if (existing.isPresent()) {
      saveRepository.delete(existing.get());
      saved = false;
    } else {
      saveRepository.save(new CareerFeedSave(userId, item, LocalDateTime.now()));
      saved = true;
    }

    return ResponseEntity.ok(mapItem(item, saved));
  }

  private Long getUserId(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return user.getId();
  }

  private void ensureCreator(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.ADMIN && user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Creator access required");
    }
  }

  private CareerFeedItemResponse mapItem(CareerFeedItem item, boolean saved) {
    return new CareerFeedItemResponse(
        item.getId(),
        item.getTitle(),
        item.getCreator(),
        item.getDescription(),
        item.getCategory().name(),
        item.getSourceUrl(),
        item.getThumbnailUrl(),
        item.getCreatedAt(),
        saved
    );
  }

  private CareerCategory parseCategory(String value) {
    if (value == null) return CareerCategory.GENERAL;
    String normalized = value.trim().toUpperCase().replace("/", "_").replace(" ", "_");
    return switch (normalized) {
      case "WEB_DEVELOPMENT" -> CareerCategory.WEB_DEVELOPMENT;
      case "AI_ML", "AI__ML" -> CareerCategory.AI_ML;
      case "INTERNSHIPS" -> CareerCategory.INTERNSHIPS;
      case "PLACEMENTS" -> CareerCategory.PLACEMENTS;
      default -> CareerCategory.GENERAL;
    };
  }
}
