package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudentIdAndCourseIdAndStatusIgnoreCase(
            Long studentId, Long courseId, String status);

    List<Enrollment> findByStudentIdAndStatusIgnoreCase(Long studentId, String status);

    List<Enrollment> findByStudentIdAndStatusIn(Long studentId, Collection<String> statuses);

    List<Enrollment> findByMentorIdAndStatusIn(Long mentorId, Collection<String> statuses);

    boolean existsByStudentIdAndCourseIdAndStatusIgnoreCase(
            Long studentId, Long courseId, String status);

        boolean existsByStudentIdAndCourseIdAndStatusIn(
            Long studentId, Long courseId, Collection<String> statuses);
}