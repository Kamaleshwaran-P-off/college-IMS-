package com.smartcampus.platform.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.feed.entity.MicroFeedItem;

public interface MicroFeedItemRepository extends JpaRepository<MicroFeedItem, Long> {}
