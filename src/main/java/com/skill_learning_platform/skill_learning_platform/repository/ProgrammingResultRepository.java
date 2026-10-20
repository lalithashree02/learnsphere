package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.ProgrammingResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgrammingResultRepository
        extends JpaRepository<ProgrammingResult, Long> {

    List<ProgrammingResult> findByMentorId(Long mentorId);
    
}