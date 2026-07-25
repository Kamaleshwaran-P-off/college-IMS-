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
import com.smartcampus.platform.feed.dto.MicroFeedCreateRequest;
import com.smartcampus.platform.feed.dto.MicroFeedItemResponse;
import com.smartcampus.platform.feed.entity.MicroFeedItem;
import com.smartcampus.platform.feed.entity.MicroFeedLike;
import com.smartcampus.platform.feed.entity.MicroFeedSave;
import com.smartcampus.platform.feed.entity.MicroFeedType;
import com.smartcampus.platform.feed.repository.MicroFeedItemRepository;
import com.smartcampus.platform.feed.repository.MicroFeedLikeRepository;
import com.smartcampus.platform.feed.repository.MicroFeedSaveRepository;

@RestController
@RequestMapping("/api/micro-feed")
public class MicroFeedController {
  private final MicroFeedItemRepository itemRepository;
  private final MicroFeedSaveRepository saveRepository;
  private final MicroFeedLikeRepository likeRepository;
  private final UserRepository userRepository;

  public MicroFeedController(
      MicroFeedItemRepository itemRepository,
      MicroFeedSaveRepository saveRepository,
      MicroFeedLikeRepository likeRepository,
      UserRepository userRepository
  ) {
    this.itemRepository = itemRepository;
    this.saveRepository = saveRepository;
    this.likeRepository = likeRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/items")
  public List<MicroFeedItemResponse> getItems(Authentication authentication) {
    Long userId = getUserId(authentication);
    Set<Long> saved = saveRepository.findByUserId(userId)
        .stream()
        .map(save -> save.getItem().getId())
        .collect(java.util.stream.Collectors.toSet());
    Set<Long> liked = likeRepository.findByUserId(userId)
        .stream()
        .map(like -> like.getItem().getId())
        .collect(java.util.stream.Collectors.toSet());

    return itemRepository.findAll()
        .stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .map(item -> mapItem(item, saved.contains(item.getId()), liked.contains(item.getId())))
        .toList();
  }

  @GetMapping("/saved")
  public List<MicroFeedItemResponse> getSaved(Authentication authentication) {
    Long userId = getUserId(authentication);
    return saveRepository.findByUserId(userId)
        .stream()
        .map(save -> mapItem(save.getItem(), true, false))
        .toList();
  }

  @PostMapping("/items")
  public ResponseEntity<MicroFeedItemResponse> createItem(
      Authentication authentication,
      @RequestBody MicroFeedCreateRequest request
  ) {
    ensureCreator(authentication);
    MicroFeedType type = parseType(request.getType());

    MicroFeedItem item = new MicroFeedItem(
        request.getTitle(),
        request.getDescription(),
        type,
        request.getVideoUrl(),
        LocalDateTime.now()
    );

    MicroFeedItem saved = itemRepository.save(item);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapItem(saved, false, false));
  }

  @PostMapping("/items/{id}/save")
  public ResponseEntity<MicroFeedItemResponse> toggleSave(Authentication authentication, @PathVariable Long id) {
    Long userId = getUserId(authentication);
    MicroFeedItem item = itemRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

    var existing = saveRepository.findByUserIdAndItemId(userId, id);
    boolean saved;
    if (existing.isPresent()) {
      saveRepository.delete(existing.get());
      saved = false;
    } else {
      saveRepository.save(new MicroFeedSave(userId, item, LocalDateTime.now()));
      saved = true;
    }

    boolean liked = likeRepository.findByUserIdAndItemId(userId, id).isPresent();
    return ResponseEntity.ok(mapItem(item, saved, liked));
  }

  @PostMapping("/items/{id}/like")
  public ResponseEntity<MicroFeedItemResponse> toggleLike(Authentication authentication, @PathVariable Long id) {
    Long userId = getUserId(authentication);
    MicroFeedItem item = itemRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

    var existing = likeRepository.findByUserIdAndItemId(userId, id);
    boolean liked;
    if (existing.isPresent()) {
      likeRepository.delete(existing.get());
      liked = false;
    } else {
      likeRepository.save(new MicroFeedLike(userId, item, LocalDateTime.now()));
      liked = true;
    }

    boolean saved = saveRepository.findByUserIdAndItemId(userId, id).isPresent();
    return ResponseEntity.ok(mapItem(item, saved, liked));
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

  private MicroFeedItemResponse mapItem(MicroFeedItem item, boolean saved, boolean liked) {
    return new MicroFeedItemResponse(
        item.getId(),
        item.getTitle(),
        item.getDescription(),
        item.getType().name(),
        item.getVideoUrl(),
        item.getCreatedAt(),
        saved,
        liked
    );
  }

  private MicroFeedType parseType(String value) {
    if (value == null) return MicroFeedType.TEXT;
    String normalized = value.trim().toUpperCase();
    return "VIDEO".equals(normalized) ? MicroFeedType.VIDEO : MicroFeedType.TEXT;
  }
}
