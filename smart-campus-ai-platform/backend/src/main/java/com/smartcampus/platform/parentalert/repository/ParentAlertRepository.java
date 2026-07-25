package com.smartcampus.platform.parentalert.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.parentalert.entity.ParentAlert;

public interface ParentAlertRepository extends JpaRepository<ParentAlert, Long> {
  boolean existsByStudentIdAndAlertDate(Long studentId, LocalDate alertDate);
}
