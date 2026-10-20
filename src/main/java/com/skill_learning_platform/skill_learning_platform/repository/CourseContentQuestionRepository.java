package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.CourseContentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseContentQuestionRepository extends JpaRepository<CourseContentQuestion, Long> {
    List<CourseContentQuestion> findByContentIdOrderByIdAsc(Long contentId);
    void deleteByContentId(Long contentId);
}