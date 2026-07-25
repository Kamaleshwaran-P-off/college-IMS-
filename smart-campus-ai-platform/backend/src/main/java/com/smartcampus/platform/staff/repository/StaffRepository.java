package com.smartcampus.platform.staff.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.staff.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long> {
  boolean existsByUserId(Long userId);
  java.util.Optional<Staff> findByUserId(Long userId);
}
