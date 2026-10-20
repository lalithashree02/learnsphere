package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.CourseContentTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseContentTestResultRepository extends JpaRepository<CourseContentTestResult, Long> {
    Optional<CourseContentTestResult> findByStudentIdAndContentId(Long studentId, Long contentId);
    void deleteByContentId(Long contentId);
}