package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.CourseContentQuestion;
import com.skill_learning_platform.skill_learning_platform.model.CourseContentQuestionView;
import com.skill_learning_platform.skill_learning_platform.model.CourseContentTestResult;
import com.skill_learning_platform.skill_learning_platform.model.CourseTestSubmission;
import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;
import com.skill_learning_platform.skill_learning_platform.service.CourseContentService;
import com.skill_learning_platform.skill_learning_platform.service.CourseService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import com.skill_learning_platform.skill_learning_platform.service.StudentTopicProgressService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/course-content")
@CrossOrigin(origins = "*")
public class CourseContentController {

    private final CourseContentService contentService;
    private final CourseService courseService;
    private final StudentTopicProgressService progressService;
    private final SessionAccessService sessionAccessService;

    public CourseContentController(
            CourseContentService contentService,
            CourseService courseService,
            StudentTopicProgressService progressService,
            SessionAccessService sessionAccessService) {

        this.contentService = contentService;
        this.courseService = courseService;
        this.progressService = progressService;
        this.sessionAccessService = sessionAccessService;
    }

    // ============================================================
    // GET ALL CONTENT FOR A COURSE
    // ============================================================

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseTopic>> getCourseContent(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long mentorId,
            @RequestParam(required = false) Long studentId,
            HttpSession session) {

        // Mentor wants to view/manage all content
        if (mentorId != null) {

            sessionAccessService.requireMentor(session, mentorId);

            requireMentorOwner(courseId, mentorId);

            return ResponseEntity.ok(
                    contentService.getCourseContent(courseId)
            );
        }

        // Student gets only the current available content
        sessionAccessService.requireStudent(session, studentId);

        CourseTopic current =
                contentService.getCurrentContent(studentId, courseId);

        if (current == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(List.of(current));
    }

    // ============================================================
    // GET CURRENT CONTENT FOR STUDENT
    // ============================================================

    @GetMapping("/course/{courseId}/current")
    public ResponseEntity<CourseTopic> getCurrentContent(
            @PathVariable Long courseId,
            @RequestParam Long studentId,
            HttpSession session) {

        sessionAccessService.requireStudent(session, studentId);

        CourseTopic current =
                contentService.getCurrentContent(studentId, courseId);

        if (current == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(current);
    }

    // ============================================================
    // GET ONE CONTENT
    // ============================================================

    @GetMapping("/{contentId}")
    public ResponseEntity<CourseTopic> getContent(
            @PathVariable Long contentId,
            @RequestParam(required = false) Long mentorId,
            @RequestParam(required = false) Long studentId,
            HttpSession session) {

        CourseTopic content =
                contentService.getContent(contentId);

        // Mentor access
        if (mentorId != null) {

            sessionAccessService.requireMentor(session, mentorId);

            requireMentorOwner(
                    content.getCourseId(),
                    mentorId
            );

        } else {

            // Student access
            sessionAccessService.requireStudent(session, studentId);

            contentService.assertStudentCanRead(
                    content,
                    studentId
            );
        }

        return ResponseEntity.ok(content);
    }

    // ============================================================
    // CREATE CONTENT
    // ============================================================

    @PostMapping
    public ResponseEntity<CourseTopic> createContent(
            @RequestBody CourseTopic content,
            @RequestParam Long mentorId,
            HttpSession session) {

        sessionAccessService.requireMentor(
                session,
                mentorId
        );

        if (content.getCourseId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course ID is required."
            );
        }

        requireMentorOwner(
                content.getCourseId(),
                mentorId
        );

        return ResponseEntity.ok(
                contentService.createContent(
                        content,
                        content.getCourseId()
                )
        );
    }

    // ============================================================
    // UPDATE CONTENT
    // ============================================================

    @PutMapping("/{contentId}")
    public ResponseEntity<CourseTopic> updateContent(
            @PathVariable Long contentId,
            @RequestBody CourseTopic updated,
            @RequestParam Long mentorId,
            HttpSession session) {

        sessionAccessService.requireMentor(
                session,
                mentorId
        );

        CourseTopic existing =
                contentService.getContent(contentId);

        requireMentorOwner(
                existing.getCourseId(),
                mentorId
        );

        return ResponseEntity.ok(
                contentService.updateContent(
                        contentId,
                        updated
                )
        );
    }

    // ============================================================
    // DELETE CONTENT
    // ============================================================

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteContent(
            @PathVariable Long contentId,
            @RequestParam Long mentorId,
            HttpSession session) {

        sessionAccessService.requireMentor(
                session,
                mentorId
        );

        CourseTopic existing =
                contentService.getContent(contentId);

        requireMentorOwner(
                existing.getCourseId(),
                mentorId
        );

        contentService.deleteContent(contentId);

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // COMPLETE TEXT CONTENT
    // ============================================================

    @PostMapping("/{contentId}/complete")
    public ResponseEntity<?> completeText(
            @PathVariable Long contentId,
            @RequestParam Long studentId,
            HttpSession session) {

        sessionAccessService.requireStudent(
                session,
                studentId
        );

        CourseTopic content =
                contentService.getContent(contentId);

        contentService.assertStudentCanRead(
                content,
                studentId
        );

        if (!"TEXT".equalsIgnoreCase(
                content.getContentType())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Submit the test to complete this content."
            );
        }

        return ResponseEntity.ok(
                progressService.markTopicCompleted(
                        studentId,
                        content.getCourseId(),
                        contentId
                )
        );
    }

    // ============================================================
    // ADD QUESTION TO CONTENT
    // ============================================================

    @PostMapping("/{contentId}/questions")
    public ResponseEntity<CourseContentQuestion> addQuestion(
            @PathVariable Long contentId,
            @RequestBody CourseContentQuestion question,
            @RequestParam Long mentorId,
            HttpSession session) {

        sessionAccessService.requireMentor(
                session,
                mentorId
        );

        CourseTopic content =
                contentService.getContent(contentId);

        requireMentorOwner(
                content.getCourseId(),
                mentorId
        );

        return ResponseEntity.ok(
                contentService.addQuestion(
                        contentId,
                        question
                )
        );
    }

    // ============================================================
    // GET QUESTIONS
    // ============================================================

    @GetMapping("/{contentId}/questions")
    public ResponseEntity<?> getQuestions(
            @PathVariable Long contentId,
            @RequestParam(required = false) Long mentorId,
            @RequestParam(required = false) Long studentId,
            HttpSession session) {

        CourseTopic content =
                contentService.getContent(contentId);

        List<CourseContentQuestion> questions;

        // Mentor gets complete questions including correct answers
        if (mentorId != null) {

            sessionAccessService.requireMentor(
                    session,
                    mentorId
            );

            requireMentorOwner(
                    content.getCourseId(),
                    mentorId
            );

            questions =
                    contentService.getQuestions(contentId);

            return ResponseEntity.ok(questions);
        }

        // Student gets questions without correct answers
        sessionAccessService.requireStudent(
                session,
                studentId
        );

        questions =
                contentService.getQuestionsForStudent(
                        contentId,
                        studentId
                );

        return ResponseEntity.ok(
                questions.stream()
                        .map(question ->
                                CourseContentQuestionView.from(question))
                        .toList()
        );
    }

    // ============================================================
    // SUBMIT TEST
    // ============================================================

    @PostMapping("/{contentId}/submit")
    public ResponseEntity<CourseContentTestResult> submitTest(
            @PathVariable Long contentId,
            @RequestBody CourseTestSubmission submission,
            @RequestParam Long studentId,
            HttpSession session) {

        sessionAccessService.requireStudent(
                session,
                studentId
        );

        return ResponseEntity.ok(
                contentService.submitTest(
                        studentId,
                        contentId,
                        submission.getAnswers()
                )
        );
    }

    // ============================================================
    // CHECK COURSE OWNERSHIP
    // ============================================================

    private void requireMentorOwner(
            Long courseId,
            Long mentorId) {

        if (courseId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course ID is required."
            );
        }

        boolean ownsCourse =
                courseService
                        .getCoursesByMentor(mentorId)
                        .stream()
                        .anyMatch(course ->
                                courseId.equals(course.getId()));

        if (!ownsCourse) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the course mentor can manage its content."
            );
        }
    }
}