package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.StudentTopicProgress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentTopicProgressRepository
        extends JpaRepository<StudentTopicProgress, Long> {

    Optional<StudentTopicProgress>
    findByStudentIdAndTopicId(
            Long studentId,
            Long topicId
    );

    List<StudentTopicProgress>
    findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );

        boolean existsByTopicId(Long topicId);
}
