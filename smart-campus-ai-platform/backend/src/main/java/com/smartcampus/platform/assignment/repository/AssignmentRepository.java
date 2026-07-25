package com.smartcampus.platform.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.assignment.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {}
