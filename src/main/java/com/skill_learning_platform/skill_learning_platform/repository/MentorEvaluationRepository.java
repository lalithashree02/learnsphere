package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.MentorEvaluation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MentorEvaluationRepository
        extends JpaRepository<MentorEvaluation, Long> {

    // Get all evaluations of a mentor,
    // latest evaluation first
    List<MentorEvaluation> findAllByMentorIdOrderByIdDesc(Long mentorId);

    // Get one evaluation by mentor ID
    Optional<MentorEvaluation> findByMentorId(Long mentorId);

    // Get all approved mentors
    List<MentorEvaluation> findByStatusIgnoreCase(String status);
}