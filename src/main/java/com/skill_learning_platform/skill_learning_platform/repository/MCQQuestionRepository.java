package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.MCQQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MCQQuestionRepository
        extends JpaRepository<MCQQuestion, Long> {

    // Get questions based on technical skill
    List<MCQQuestion> findBySkill(String skill);
}