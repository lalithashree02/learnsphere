package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseTopicRepository
        extends JpaRepository<CourseTopic, Long> {

    List<CourseTopic> findByCourseIdOrderByTopicNumberAsc(
            Long courseId
    );
}