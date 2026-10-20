package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.*;
import com.skill_learning_platform.skill_learning_platform.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MentorFeedbackService {

    private final MentorFeedbackRepository feedbackRepository;
    private final CourseRepository courseRepository;
    private final CourseTopicRepository topicRepository;
    private final StudentTopicProgressRepository progressRepository;
    private final EnrollmentService enrollmentService;
    private final MentorRepository mentorRepository;
    private final MentorEvaluationRepository evaluationRepository;
    private final EnrollmentRepository enrollmentRepository;

    public MentorFeedbackService(
            MentorFeedbackRepository feedbackRepository,
            CourseRepository courseRepository,
            CourseTopicRepository topicRepository,
            StudentTopicProgressRepository progressRepository,
            EnrollmentService enrollmentService,
            MentorRepository mentorRepository,
            MentorEvaluationRepository evaluationRepository,
            EnrollmentRepository enrollmentRepository) {

        this.feedbackRepository = feedbackRepository;
        this.courseRepository = courseRepository;
        this.topicRepository = topicRepository;
        this.progressRepository = progressRepository;
        this.enrollmentService = enrollmentService;
        this.mentorRepository = mentorRepository;
        this.evaluationRepository = evaluationRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public MentorFeedback submit(
            MentorFeedback feedback) {

        if (
            feedback.getRating() < 1
            ||
            feedback.getRating() > 5
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rating must be from 1 to 5."
            );
        }

        Course course =
                courseRepository
                        .findById(feedback.getCourseId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Course not found."
                                )
                        );

        if (
            !enrollmentService.hasAccess(
                    feedback.getStudentId(),
                    course.getId()
            )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Active enrollment is required."
            );
        }

        List<CourseTopic> topics =
                topicRepository
                        .findByCourseIdOrderByTopicNumberAsc(
                                course.getId()
                        );

        long completed =
                progressRepository
                        .findByStudentIdAndCourseId(
                                feedback.getStudentId(),
                                course.getId()
                        )
                        .stream()
                        .filter(progress ->
                                progress.isCompleted()
                        )
                        .count();

        if (
            topics.isEmpty()
            ||
            completed < topics.size()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Complete all course topics before submitting feedback."
            );
        }

        if (
            feedbackRepository
                    .existsByStudentIdAndCourseId(
                            feedback.getStudentId(),
                            course.getId()
                    )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Feedback has already been submitted for this course."
            );
        }

        feedback.setCourseId(
                course.getId()
        );

        feedback.setMentorId(
                course.getMentorId()
        );

        feedback.setCreatedAt(
                LocalDateTime.now()
        );

        return feedbackRepository.save(
                feedback
        );
    }

    public List<MentorFeedback> getMentorFeedback(
            Long mentorId) {

        return feedbackRepository
                .findByMentorIdOrderByCreatedAtDesc(
                        mentorId
                );
    }

    public Map<String, Object> getMentorQuality(
            Long mentorId) {

        Mentor mentor =
                mentorRepository
                        .findById(mentorId)
                        .orElse(null);

        if (mentor == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Mentor not found."
            );
        }

        return qualityRecord(mentor);
    }

    public List<Map<String, Object>> getAllQuality() {

        return mentorRepository
                .findAll()
                .stream()
                .map(mentor ->
                        qualityRecord(mentor)
                )
                .toList();
    }

    private Map<String, Object> qualityRecord(
            Mentor mentor) {

        long reviewCount =
                feedbackRepository
                        .countByMentorId(
                                mentor.getId()
                        );

        Double average =
                feedbackRepository
                        .averageRatingForMentor(
                                mentor.getId()
                        );

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "mentorId",
                mentor.getId()
        );

        result.put(
                "mentorName",
                mentor.getFullName()
        );

        result.put(
                "reviewCount",
                reviewCount
        );

        result.put(
                "qualityStatus",
                reviewCount == 0
                        ? "No student reviews yet"
                        : "Student feedback available"
        );

        result.put(
                "averageRating",
                average == null
                        ? 0
                        : Math.round(
                                average * 100.0
                          ) / 100.0
        );

        result.put(
                "courseCount",
                courseRepository
                        .findByMentorId(
                                mentor.getId()
                        )
                        .size()
        );

        result.put(
                "enrolledStudentCount",
                enrollmentRepository
                        .findByMentorIdAndStatusIn(
                                mentor.getId(),
                                List.of(
                                        "ACTIVE",
                                        "COMPLETED"
                                )
                        )
                        .stream()
                        .map(enrollment ->
                                enrollment.getStudentId()
                        )
                        .distinct()
                        .count()
        );

        result.put(
                "recentFeedback",
                getMentorFeedback(
                        mentor.getId()
                )
                .stream()
                .limit(5)
                .toList()
        );

        result.put(
                "initialEvaluationScore",
                evaluationRepository
                        .findByMentorId(
                                mentor.getId()
                        )
                        .map(evaluation ->
                                evaluation.getFinalScore()
                        )
                        .orElse(null)
        );

        return result;
    }
}