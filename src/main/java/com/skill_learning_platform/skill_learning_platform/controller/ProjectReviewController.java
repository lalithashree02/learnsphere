package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.ProjectSubmission;
import com.skill_learning_platform.skill_learning_platform.service.ProjectReviewService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-reviews")
@CrossOrigin(origins = "*")
public class ProjectReviewController {

    private final ProjectReviewService projectReviewService;
        private final SessionAccessService sessionAccessService;


    public ProjectReviewController(
            ProjectReviewService projectReviewService,
            SessionAccessService sessionAccessService) {

        this.projectReviewService =
                projectReviewService;
        this.sessionAccessService = sessionAccessService;
    }


    // ==========================================
    // GET PENDING PROJECTS
    // ==========================================

    @GetMapping("/mentor/{mentorId}/pending")
    public ResponseEntity<List<ProjectSubmission>>
            getPendingProjects(
                                        @PathVariable Long mentorId, HttpSession session) {

                sessionAccessService.requireMentor(session, mentorId);

        return ResponseEntity.ok(
                projectReviewService
                        .getPendingProjects(
                                mentorId
                        )
        );
    }


    // ==========================================
    // REVIEW PROJECT
    // ==========================================

    @PutMapping("/{submissionId}")
    public ResponseEntity<?> reviewProject(

            @PathVariable Long submissionId,

            @RequestParam Long mentorId,

            @RequestParam String status,

            @RequestParam(required = false)
                        String feedback,
                        HttpSession session) {

                sessionAccessService.requireMentor(session, mentorId);

        try {

                        if (!"APPROVED".equalsIgnoreCase(status)
                                        && !"CHANGES_REQUIRED".equalsIgnoreCase(status)) {
                                return ResponseEntity.badRequest().body("Invalid review status.");
                        }

            ProjectSubmission reviewedSubmission =
                    projectReviewService.reviewProject(
                            submissionId,
                            mentorId,
                            status,
                            feedback
                    );

            return ResponseEntity.ok(
                    reviewedSubmission
            );

        } catch (IllegalArgumentException error) {

            return ResponseEntity
                    .badRequest()
                    .body(error.getMessage());

        } catch (IllegalStateException error) {

            return ResponseEntity
                    .badRequest()
                    .body(error.getMessage());
        }
    }
}