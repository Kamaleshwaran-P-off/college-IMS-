package com.smartcampus.platform.feed.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.feed.entity.MicroFeedLike;

public interface MicroFeedLikeRepository extends JpaRepository<MicroFeedLike, Long> {
  List<MicroFeedLike> findByUserId(Long userId);

  Optional<MicroFeedLike> findByUserIdAndItemId(Long userId, Long itemId);
}
