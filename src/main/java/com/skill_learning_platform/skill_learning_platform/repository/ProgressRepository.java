package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.Progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository
        extends JpaRepository<Progress, Long> {

    List<Progress> findByStudentId(Long studentId);

    Optional<Progress> findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );
}