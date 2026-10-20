package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.MCQResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MCQResultRepository extends JpaRepository<MCQResult, Long> {

    Optional<MCQResult> findByMentorId(Long mentorId);
}