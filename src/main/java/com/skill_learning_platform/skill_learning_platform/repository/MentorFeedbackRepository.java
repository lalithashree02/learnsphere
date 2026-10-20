package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.MentorFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    List<MentorFeedback> findByMentorIdOrderByCreatedAtDesc(Long mentorId);
    long countByMentorId(Long mentorId);

    @Query("select avg(f.rating) from MentorFeedback f where f.mentorId = :mentorId")
    Double averageRatingForMentor(@Param("mentorId") Long mentorId);
}