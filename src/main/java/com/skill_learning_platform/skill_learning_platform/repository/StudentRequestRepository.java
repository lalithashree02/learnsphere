package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.StudentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRequestRepository
        extends JpaRepository<StudentRequest, Long> {

    // Get all requests sent to a particular mentor
    List<StudentRequest> findByMentorId(Long mentorId);

    // Get requests of a particular status for a mentor
    List<StudentRequest> findByMentorIdAndStatus(
            Long mentorId,
            String status
    );
    boolean existsByStudentIdAndMentorId(
            Long studentId,
            Long mentorId
    );

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    List<StudentRequest> findByStudentIdOrderByIdDesc(Long studentId);

    Optional<StudentRequest> findFirstByStudentIdAndMentorIdOrderByIdDesc(
            Long studentId,
            Long mentorId
    );
}