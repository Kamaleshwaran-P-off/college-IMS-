package com.smartcampus.platform.coursework.material.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.coursework.material.entity.StudyMaterial;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
  List<StudyMaterial> findByClassNameOrderByUploadedAtDesc(String className);
  List<StudyMaterial> findByClassNameAndIsVisibleTrueOrderByUploadedAtDesc(String className);
  List<StudyMaterial> findByUploadedByIdOrderByUploadedAtDesc(Long staffId);
  List<StudyMaterial> findByUploadedByIdAndClassNameInOrderByUploadedAtDesc(Long staffId, List<String> classNames);
  List<StudyMaterial> findByClassNameIsNullOrderByUploadedAtDesc();
  List<StudyMaterial> findByClassNameIsNullAndIsVisibleTrueOrderByUploadedAtDesc();
}
