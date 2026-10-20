package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.StudentTopicProgress;
import com.skill_learning_platform.skill_learning_platform.service.StudentTopicProgressService;
import com.skill_learning_platform.skill_learning_platform.service.EnrollmentService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-topic-progress")
@CrossOrigin(origins = "*")
public class StudentTopicProgressController {

    private final StudentTopicProgressService progressService;
        private final EnrollmentService enrollmentService;
        private final SessionAccessService sessionAccessService;

    public StudentTopicProgressController(
                        StudentTopicProgressService progressService,
                            EnrollmentService enrollmentService,
                            SessionAccessService sessionAccessService) {

        this.progressService = progressService;
                this.enrollmentService = enrollmentService;
                this.sessionAccessService = sessionAccessService;
    }
    @PostMapping("/complete")
    public ResponseEntity<StudentTopicProgress>
            markTopicCompleted(

                    @RequestParam Long studentId,
                    @RequestParam Long courseId,
                    @RequestParam Long topicId,
                    HttpSession session) {

        sessionAccessService.requireStudent(session, studentId);
        requireEnrollment(studentId, courseId);

        StudentTopicProgress progress =
                progressService.markTopicCompleted(
                        studentId,
                        courseId,
                        topicId
                );

        return ResponseEntity.ok(progress);
    }
    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<List<StudentTopicProgress>>
            getCourseProgress(

                    @PathVariable Long studentId,
                    @PathVariable Long courseId,
                    HttpSession session) {

        sessionAccessService.requireStudent(session, studentId);
        requireEnrollment(studentId, courseId);

        return ResponseEntity.ok(
                progressService.getCourseProgress(
                        studentId,
                        courseId
                )
        );
    }

    // Check whether a topic is completed
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkTopicCompleted(

            @RequestParam Long studentId,
            @RequestParam Long courseId,
                        @RequestParam Long topicId,
                        HttpSession session) {

                sessionAccessService.requireStudent(session, studentId);
        requireEnrollment(studentId, courseId);

        boolean completed =
                progressService.isTopicCompleted(
                        studentId,
                        topicId,
                        courseId
                );

        return ResponseEntity.ok(completed);
    }

        private void requireEnrollment(Long studentId, Long courseId) {
                if (!enrollmentService.hasAccess(studentId, courseId)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "An active enrollment is required to access course progress.");
                }
        }
}