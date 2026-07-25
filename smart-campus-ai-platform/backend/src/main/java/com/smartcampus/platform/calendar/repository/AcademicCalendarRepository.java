package com.smartcampus.platform.calendar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.calendar.entity.AcademicCalendar;

public interface AcademicCalendarRepository extends JpaRepository<AcademicCalendar, Long> {
  Optional<AcademicCalendar> findTopByOrderByUploadedAtDesc();
}
