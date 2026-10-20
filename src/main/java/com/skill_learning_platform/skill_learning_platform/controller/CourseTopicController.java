package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;
import com.skill_learning_platform.skill_learning_platform.service.CourseTopicService;
import com.skill_learning_platform.skill_learning_platform.service.EnrollmentService;
import com.skill_learning_platform.skill_learning_platform.service.CourseService;
import com.skill_learning_platform.skill_learning_platform.service.CourseContentService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-topics")
@CrossOrigin(origins = "*")
public class CourseTopicController {

    private final CourseTopicService courseTopicService;
        private final EnrollmentService enrollmentService;
        private final CourseService courseService;
        private final SessionAccessService sessionAccessService;
        private final CourseContentService courseContentService;

    public CourseTopicController(
            CourseTopicService courseTopicService,
            EnrollmentService enrollmentService,
            CourseService courseService,
            SessionAccessService sessionAccessService,
            CourseContentService courseContentService) {

        this.courseTopicService =
                courseTopicService;
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
        this.sessionAccessService = sessionAccessService;
        this.courseContentService = courseContentService;
    }

    // Create a new topic
    @PostMapping
    public ResponseEntity<CourseTopic> createTopic(
                        @RequestBody CourseTopic topic,
                        @RequestParam Long mentorId,
                        HttpSession session) {

                sessionAccessService.requireMentor(session, mentorId);
                requireMentorOwner(topic.getCourseId(), mentorId);

        CourseTopic savedTopic =
                courseTopicService.createTopic(topic);

        return ResponseEntity.ok(savedTopic);
    }

    // Get all topics
    @GetMapping
    public ResponseEntity<List<CourseTopic>> getAllTopics() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Course context and enrollment are required to view topic content.");
    }

    // Get one topic
    @GetMapping("/{id}")
    public ResponseEntity<CourseTopic> getTopicById(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long mentorId,
            HttpSession session) {

        CourseTopic topic =
                courseTopicService.getTopicById(id);

        if (topic == null) {
            return ResponseEntity.notFound().build();
        }

                requireReadAccess(topic.getCourseId(), studentId, mentorId, session);
                if (studentId != null) {
                        courseContentService.assertStudentCanRead(topic, studentId);
                }

        return ResponseEntity.ok(topic);
    }

    // Get all topics for a course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseTopic>>
            getTopicsByCourse(
                    @PathVariable Long courseId,
                    @RequestParam(required = false) Long studentId,
                    @RequestParam(required = false) Long mentorId,
                    HttpSession session) {

        requireReadAccess(courseId, studentId, mentorId, session);

        if (studentId != null) {
                CourseTopic current = courseContentService.getCurrentContent(studentId, courseId);
                return ResponseEntity.ok(current == null ? List.of() : List.of(current));
        }

        return ResponseEntity.ok(
                courseTopicService
                        .getTopicsByCourse(courseId)
        );
    }

    // Update a topic
    @PutMapping("/{id}")
    public ResponseEntity<CourseTopic> updateTopic(
            @PathVariable Long id,
                        @RequestBody CourseTopic updatedTopic,
                        @RequestParam Long mentorId,
                        HttpSession session) {

                sessionAccessService.requireMentor(session, mentorId);
                CourseTopic existing = courseTopicService.getTopicById(id);
                if (existing == null) return ResponseEntity.notFound().build();
                requireMentorOwner(existing.getCourseId(), mentorId);
                if (!existing.getCourseId().equals(updatedTopic.getCourseId())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "A topic cannot be moved to another course.");
                }

        CourseTopic topic =
                courseTopicService.updateTopic(
                        id,
                        updatedTopic
                );

        if (topic == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(topic);
    }

    // Delete a topic
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(
                        @PathVariable Long id,
                        @RequestParam Long mentorId,
                        HttpSession session) {

                sessionAccessService.requireMentor(session, mentorId);
                CourseTopic topic = courseTopicService.getTopicById(id);
                if (topic == null) return ResponseEntity.notFound().build();
                requireMentorOwner(topic.getCourseId(), mentorId);

        boolean deleted =
                courseTopicService.deleteTopic(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

        private void requireReadAccess(Long courseId, Long studentId, Long mentorId, HttpSession session) {
                if (studentId != null) {
                        sessionAccessService.requireStudent(session, studentId);
                        if (!enrollmentService.hasAccess(studentId, courseId)) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                "An active enrollment is required to view course content.");
                        }
                } else {
                        sessionAccessService.requireMentor(session, mentorId);
                        requireMentorOwner(courseId, mentorId);
                }
        }

        private void requireMentorOwner(Long courseId, Long mentorId) {
                if (courseId == null || mentorId == null || courseService.getCoursesByMentor(mentorId)
                                .stream().noneMatch(course -> course.getId().equals(courseId))) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Only the course mentor can manage topic content.");
                }
        }
}