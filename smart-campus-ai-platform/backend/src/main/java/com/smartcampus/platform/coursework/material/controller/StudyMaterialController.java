package com.smartcampus.platform.coursework.material.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartcampus.platform.coursework.material.dto.StudyMaterialResponse;
import com.smartcampus.platform.coursework.material.entity.StudyMaterial;
import com.smartcampus.platform.coursework.material.service.StudyMaterialService;

@RestController
@RequestMapping("/api/coursework/materials")
@Validated
public class StudyMaterialController {
  private static final Logger log = LoggerFactory.getLogger(StudyMaterialController.class);
  private final StudyMaterialService materialService;

  public StudyMaterialController(StudyMaterialService materialService) {
    this.materialService = materialService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public StudyMaterialResponse uploadMaterial(
      Authentication authentication,
      @RequestParam("title") String title,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "department", required = false) String department,
      @RequestParam(value = "className", required = false) String className,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    log.info("Study material upload: title={}, class={}, fileName={}, size={}, type={}",
        title,
        className,
        file != null ? file.getOriginalFilename() : "none",
        file != null ? file.getSize() : 0,
        file != null ? file.getContentType() : "none");
    return materialService.uploadMaterial(
        title,
        description,
        department,
        className,
        file,
        authentication.getName()
    );
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public StudyMaterialResponse updateMaterial(
      Authentication authentication,
      @PathVariable Long id,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "department", required = false) String department,
      @RequestParam(value = "className", required = false) String className,
      @RequestParam(value = "file", required = false) MultipartFile file
  ) throws IOException {
    return materialService.updateMaterial(
        id,
        title,
        description,
        department,
        className,
        file,
        authentication.getName()
    );
  }

  @PatchMapping("/{id}/hide")
  public StudyMaterialResponse updateVisibility(
      Authentication authentication,
      @PathVariable Long id,
      @RequestParam(value = "visible", required = false) Boolean visible
  ) {
    return materialService.updateVisibility(id, visible, authentication.getName());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMaterial(
      Authentication authentication,
      @PathVariable Long id
  ) {
    materialService.deleteMaterial(id, authentication.getName());
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public List<StudyMaterialResponse> getMaterials(Authentication authentication) {
    return materialService.getMaterials(authentication.getName());
  }

  @GetMapping("/{id}/file")
  public ResponseEntity<byte[]> downloadMaterial(
      Authentication authentication,
      @PathVariable Long id
  ) {
    StudyMaterial material = materialService.getMaterialForDownload(id, authentication.getName());
    if (material.getFileData() == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + material.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(material.getContentType()))
        .body(material.getFileData());
  }
}
