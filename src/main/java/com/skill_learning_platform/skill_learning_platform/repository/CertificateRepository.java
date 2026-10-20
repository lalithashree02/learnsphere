package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.Certificate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository
        extends JpaRepository<Certificate, Long> {

    List<Certificate> findByStudentId(Long studentId);

    List<Certificate> findByMentorId(Long mentorId);

    Optional<Certificate> findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );

    Optional<Certificate> findByCertificateNumber(
            String certificateNumber
    );
}