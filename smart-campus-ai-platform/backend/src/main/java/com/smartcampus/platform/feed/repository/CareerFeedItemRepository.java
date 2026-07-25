package com.smartcampus.platform.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.feed.entity.CareerFeedItem;

public interface CareerFeedItemRepository extends JpaRepository<CareerFeedItem, Long> {}
