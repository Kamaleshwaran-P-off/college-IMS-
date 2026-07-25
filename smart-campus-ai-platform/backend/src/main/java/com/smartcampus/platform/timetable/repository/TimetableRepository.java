package com.smartcampus.platform.timetable.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.timetable.entity.Timetable;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
  Optional<Timetable> findTopByDepartmentAndSectionOrderByUploadedAtDesc(String department, String section);
}
