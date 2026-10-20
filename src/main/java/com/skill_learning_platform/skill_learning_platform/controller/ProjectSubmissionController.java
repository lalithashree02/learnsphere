package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.ProjectSubmission;
import com.skill_learning_platform.skill_learning_platform.service.ProjectSubmissionService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-submissions")
@CrossOrigin(origins = "*")
public class ProjectSubmissionController {

    private final ProjectSubmissionService projectSubmissionService;
        private final SessionAccessService sessionAccessService;

    public ProjectSubmissionController(
            ProjectSubmissionService projectSubmissionService,
            SessionAccessService sessionAccessService) {

        this.projectSubmissionService =
                projectSubmissionService;
        this.sessionAccessService = sessionAccessService;
    }


    // ==========================================
    // SUBMIT FINAL PROJECT
    // ==========================================

    @PostMapping
    public ResponseEntity<?> submitProject(
                        @RequestBody ProjectSubmission submission, HttpSession session) {

                sessionAccessService.requireStudent(session, submission.getStudentId());

        try {

            ProjectSubmission savedSubmission =
                    projectSubmissionService
                            .submitProject(submission);

            return ResponseEntity.ok(
                    savedSubmission
            );

        } catch (IllegalStateException error) {

            return ResponseEntity
                    .badRequest()
                    .body(error.getMessage());
        }
    }


    // ==========================================
    // GET STUDENT SUBMISSIONS
    // ==========================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ProjectSubmission>>
            getStudentSubmissions(
                                        @PathVariable Long studentId, HttpSession session) {

                sessionAccessService.requireStudent(session, studentId);

        return ResponseEntity.ok(
                projectSubmissionService
                        .getStudentSubmissions(
                                studentId
                        )
        );
    }


    // ==========================================
    // GET MENTOR SUBMISSIONS
    // ==========================================

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<ProjectSubmission>>
            getMentorSubmissions(
                                        @PathVariable Long mentorId, HttpSession session) {

                sessionAccessService.requireMentor(session, mentorId);

        return ResponseEntity.ok(
                projectSubmissionService
                        .getMentorSubmissions(
                                mentorId
                        )
        );
    }


    // ==========================================
    // GET ONE SUBMISSION
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<ProjectSubmission>
            getSubmissionById(
                    @PathVariable Long id, HttpSession session) {

        ProjectSubmission submission =
                projectSubmissionService
                        .getSubmissionById(id);

        if (submission == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        Object studentId = session.getAttribute("studentId");
        Object mentorId = session.getAttribute("mentorId");
        boolean isStudent = studentId != null && submission.getStudentId() != null
                && studentId.toString().equals(submission.getStudentId().toString());
        boolean isMentor = mentorId != null && submission.getMentorId() != null
                && mentorId.toString().equals(submission.getMentorId().toString());
        if (!isStudent && !isMentor) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                submission
        );
    }
}
