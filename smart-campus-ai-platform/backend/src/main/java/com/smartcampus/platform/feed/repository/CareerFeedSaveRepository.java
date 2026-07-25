package com.smartcampus.platform.feed.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.feed.entity.CareerFeedSave;

public interface CareerFeedSaveRepository extends JpaRepository<CareerFeedSave, Long> {
  List<CareerFeedSave> findByUserId(Long userId);

  Optional<CareerFeedSave> findByUserIdAndItemId(Long userId, Long itemId);
}
