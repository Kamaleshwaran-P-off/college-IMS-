package com.smartcampus.platform.feed.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.feed.entity.MicroFeedSave;

public interface MicroFeedSaveRepository extends JpaRepository<MicroFeedSave, Long> {
  List<MicroFeedSave> findByUserId(Long userId);

  Optional<MicroFeedSave> findByUserIdAndItemId(Long userId, Long itemId);
}
