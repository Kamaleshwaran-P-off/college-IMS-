package com.smartcampus.platform.profile.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.profile.dto.UserProfileResponse;
import com.smartcampus.platform.profile.dto.UserProfileUpdateRequest;
import com.smartcampus.platform.profile.service.UserProfileService;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {
  private final UserProfileService profileService;

  public UserProfileController(UserProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  public UserProfileResponse getProfile(Authentication authentication) {
    return profileService.getProfile(authentication.getName());
  }

  @PostMapping
  public UserProfileResponse updateProfile(
      Authentication authentication,
      @RequestBody UserProfileUpdateRequest request
  ) {
    return profileService.updateProfile(authentication.getName(), request);
  }

  @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, String>> uploadImage(
      Authentication authentication,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
    }
    User user = profileService.updateProfileImage(
        authentication.getName(),
        file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType(),
        file.getBytes()
    );
    return ResponseEntity.ok(Map.of("url", user.getProfileImageUrl()));
  }

  @GetMapping("/image/{id}")
  public ResponseEntity<byte[]> getImage(Authentication authentication, @PathVariable Long id) {
    User user = profileService.loadUserForImage(id, authentication.getName());
    if (user.getProfileImageData() == null || user.getProfileImageData().length == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile image not found");
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"profile-" + id + "\"")
        .contentType(MediaType.parseMediaType(
            user.getProfileImageContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : user.getProfileImageContentType()
        ))
        .body(user.getProfileImageData());
  }
}
