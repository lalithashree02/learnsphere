package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.Course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository
        extends JpaRepository<Course, Long> {

    List<Course> findByMentorId(Long mentorId);

    List<Course> findBySkillIgnoreCase(String skill);
}