package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.ProjectSubmission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectSubmissionRepository
        extends JpaRepository<ProjectSubmission, Long> {

    List<ProjectSubmission> findByStudentId(Long studentId);

    List<ProjectSubmission> findByMentorId(Long mentorId);

    Optional<ProjectSubmission> findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );

    List<ProjectSubmission> findByMentorIdAndStatus(
            Long mentorId,
            String status
    );
}