package com.smartcampus.platform.carousel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.carousel.entity.CarouselImage;

public interface CarouselImageRepository extends JpaRepository<CarouselImage, Long> {
  List<CarouselImage> findAllByOrderByCreatedAtDesc();
}
