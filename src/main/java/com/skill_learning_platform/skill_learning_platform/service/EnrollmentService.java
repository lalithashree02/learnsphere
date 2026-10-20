package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Enrollment;
import com.skill_learning_platform.skill_learning_platform.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public boolean isActive(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIgnoreCase(
                studentId, courseId, "ACTIVE");
    }

    public boolean hasAccess(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                studentId, courseId, List.of("ACTIVE", "COMPLETED"));
    }

    public List<Enrollment> getActiveForStudent(Long studentId) {
        return enrollmentRepository.findByStudentIdAndStatusIn(
                studentId, List.of("ACTIVE", "COMPLETED"));
    }

    public Enrollment enroll(Long studentId, Long courseId, Long mentorId) {
        return enrollmentRepository
                .findByStudentIdAndCourseIdAndStatusIgnoreCase(studentId, courseId, "ACTIVE")
                .orElseGet(() -> enrollmentRepository.save(
                        new Enrollment(studentId, courseId, mentorId)));
    }

    public void markCompleted(Long studentId, Long courseId) {
        enrollmentRepository.findByStudentIdAndCourseIdAndStatusIgnoreCase(
                studentId, courseId, "ACTIVE").ifPresent(enrollment -> {
                    enrollment.setStatus("COMPLETED");
                    enrollmentRepository.save(enrollment);
                });
    }
}