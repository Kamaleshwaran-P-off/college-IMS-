package com.smartcampus.platform.carousel.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.smartcampus.platform.carousel.dto.CarouselItemResponse;
import com.smartcampus.platform.carousel.entity.CarouselImage;
import com.smartcampus.platform.carousel.repository.CarouselImageRepository;

@RestController
@RequestMapping("/api")
public class CarouselController {
  private final CarouselImageRepository carouselImageRepository;

  public CarouselController(CarouselImageRepository carouselImageRepository) {
    this.carouselImageRepository = carouselImageRepository;
  }

  @GetMapping("/carousel")
  public List<CarouselItemResponse> getCarousel() {
    return carouselImageRepository.findAllByOrderByCreatedAtDesc()
        .stream()
        .map(image -> new CarouselItemResponse(
            image.getId(),
            "/api/carousel/" + image.getId() + "/image",
            image.getCreatedAt()
        ))
        .toList();
  }

  @GetMapping("/carousel/{id}/image")
  public ResponseEntity<byte[]> getCarouselImage(@PathVariable Long id) {
    CarouselImage image = carouselImageRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(image.getContentType()))
        .body(image.getImageData());
  }
}
