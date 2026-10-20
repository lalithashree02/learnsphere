package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.ProgrammingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgrammingQuestionRepository
        extends JpaRepository<ProgrammingQuestion, Long> {

    List<ProgrammingQuestion> findBySkill(String skill);
}