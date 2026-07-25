package com.smartcampus.platform.carousel.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.carousel.entity.CarouselImage;
import com.smartcampus.platform.carousel.repository.CarouselImageRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminCarouselController {
  private static final Logger log = LoggerFactory.getLogger(AdminCarouselController.class);
  private final UserRepository userRepository;
  private final CarouselImageRepository carouselImageRepository;

  public AdminCarouselController(UserRepository userRepository, CarouselImageRepository carouselImageRepository) {
    this.userRepository = userRepository;
    this.carouselImageRepository = carouselImageRepository;
  }

  @PostMapping(value = {"/carousel", "/carousel/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> uploadCarousel(
      Authentication authentication,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    ensureAdmin(authentication);
    log.info("Carousel upload received: name={}, size={}, type={}",
        file != null ? file.getOriginalFilename() : "null",
        file != null ? file.getSize() : 0,
        file != null ? file.getContentType() : "null");
    if (file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
    }

    CarouselImage image = new CarouselImage(
        file.getOriginalFilename(),
        file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType(),
        file.getBytes(),
        LocalDateTime.now()
    );

    CarouselImage saved = carouselImageRepository.save(image);
    return ResponseEntity.ok(Map.of("id", saved.getId()));
  }

  @DeleteMapping("/carousel/{id}")
  public ResponseEntity<Void> deleteCarousel(Authentication authentication, @PathVariable Long id) {
    ensureAdmin(authentication);
    if (!carouselImageRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
    }
    carouselImageRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private void ensureAdmin(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }
  }
}
