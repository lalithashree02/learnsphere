package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.StudentTopicProgress;
import com.skill_learning_platform.skill_learning_platform.repository.StudentTopicProgressRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseTopicRepository;
import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentTopicProgressService {

    private final StudentTopicProgressRepository progressRepository;
    private final CourseTopicRepository topicRepository;
    private final EnrollmentService enrollmentService;

    public StudentTopicProgressService(
            StudentTopicProgressRepository progressRepository,
            CourseTopicRepository topicRepository,
            EnrollmentService enrollmentService) {

        this.progressRepository = progressRepository;
        this.topicRepository = topicRepository;
        this.enrollmentService = enrollmentService;
    }

    // Mark a topic as completed
    public StudentTopicProgress markTopicCompleted(
            Long studentId,
            Long courseId,
            Long topicId) {

        return markCompleted(studentId, courseId, topicId, false);
    }

    public StudentTopicProgress markTestCompleted(
            Long studentId,
            Long courseId,
            Long topicId) {

        return markCompleted(studentId, courseId, topicId, true);
    }

    private StudentTopicProgress markCompleted(
            Long studentId,
            Long courseId,
            Long topicId,
            boolean testSubmission) {

        CourseTopic topic =
                topicRepository
                        .findById(topicId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Topic not found."
                                )
                        );

        if (
            !courseId.equals(
                    topic.getCourseId()
            )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Topic does not belong to the requested course."
            );
        }

        if (testSubmission != "QUESTION_TEST".equalsIgnoreCase(topic.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Content must be completed using its required learning activity.");
        }

        CourseTopic currentTopic = topicRepository
                .findByCourseIdOrderByTopicNumberAsc(courseId)
                .stream()
                .filter(candidate -> !isTopicCompleted(studentId, candidate.getId(), courseId))
                .findFirst()
                .orElse(null);
        if (currentTopic == null || !currentTopic.getId().equals(topicId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Complete course content in sequence.");
        }

        StudentTopicProgress progress =
                progressRepository
                        .findByStudentIdAndTopicId(
                                studentId,
                                topicId
                        )
                        .orElse(null);

        // If progress already exists, update it
        if (progress != null) {

            progress.setCompleted(true);

            StudentTopicProgress saved =
                    progressRepository.save(progress);

            updateCourseCompletion(
                    studentId,
                    courseId
            );

            return saved;
        }

        // Otherwise create new progress record
        progress =
                new StudentTopicProgress(
                        studentId,
                        courseId,
                        topicId,
                        true
                );

        StudentTopicProgress saved =
                progressRepository.save(progress);

        updateCourseCompletion(
                studentId,
                courseId
        );

        return saved;
    }

    // Get all progress for a student's course
    public List<StudentTopicProgress> getCourseProgress(
            Long studentId,
            Long courseId) {

        return progressRepository
                .findByStudentIdAndCourseId(
                        studentId,
                        courseId
                );
    }

    // Check whether a particular topic is completed
    public boolean isTopicCompleted(
            Long studentId,
            Long topicId,
            Long courseId) {

        CourseTopic topic =
                topicRepository
                        .findById(topicId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Topic not found."
                                )
                        );

        if (
            !courseId.equals(
                    topic.getCourseId()
            )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Topic does not belong to the requested course."
            );
        }

        StudentTopicProgress progress =
                progressRepository
                        .findByStudentIdAndTopicId(
                                studentId,
                                topicId
                        )
                        .orElse(null);

        return progress != null
                && progress.isCompleted();
    }

    private void updateCourseCompletion(
            Long studentId,
            Long courseId) {

        List<CourseTopic> topics =
                topicRepository
                        .findByCourseIdOrderByTopicNumberAsc(
                                courseId
                        );

        if (topics.isEmpty()) {
            return;
        }

        long completed =
                progressRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .stream()
                        .filter(progress ->
                                progress.isCompleted()
                        )
                        .count();

        if (
            completed >= topics.size()
        ) {

            enrollmentService.markCompleted(
                    studentId,
                    courseId
            );
        }
    }
}