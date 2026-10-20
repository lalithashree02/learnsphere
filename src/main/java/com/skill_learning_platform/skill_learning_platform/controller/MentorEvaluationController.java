package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.MentorEvaluation;
import com.skill_learning_platform.skill_learning_platform.service.MentorEvaluationService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentor-evaluations")
@CrossOrigin(origins = "*")
public class MentorEvaluationController {

    private final MentorEvaluationService mentorEvaluationService;
    private final SessionAccessService sessionAccessService;

    public MentorEvaluationController(
            MentorEvaluationService mentorEvaluationService,
            SessionAccessService sessionAccessService) {

        this.mentorEvaluationService =
                mentorEvaluationService;
        this.sessionAccessService = sessionAccessService;
    }


    // ==========================================
    // GET EVALUATION OF A MENTOR
    // ==========================================

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<MentorEvaluation> getEvaluation(
            @PathVariable Long mentorId, HttpSession session) {

        if (session.getAttribute("adminId") == null) {
            sessionAccessService.requireMentor(session, mentorId);
        }

        MentorEvaluation evaluation =
                mentorEvaluationService
                        .getEvaluationByMentorId(mentorId);

        if (evaluation == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(evaluation);
    }


    // ==========================================
    // ADMIN SAVES ROUND 3 SCORES
    // ==========================================

    @PostMapping
    public ResponseEntity<MentorEvaluation> saveEvaluation(
            @RequestBody MentorEvaluation evaluation,
            jakarta.servlet.http.HttpSession session) {

        // Check whether admin is logged in
        if (session.getAttribute("adminId") == null) {
            return ResponseEntity.status(403).build();
        }

        try {

            MentorEvaluation savedEvaluation =
                    mentorEvaluationService
                            .saveEvaluation(evaluation);

            return ResponseEntity.ok(savedEvaluation);

        } catch (IllegalArgumentException error) {

            return ResponseEntity.badRequest().build();

        } catch (IllegalStateException error) {

            return ResponseEntity.status(409).build();
        }
    }


    // ==========================================
    // ADMIN APPROVES MENTOR
    // ==========================================

    @PostMapping("/{mentorId}/approve")
    public ResponseEntity<MentorEvaluation> approveMentor(
            @PathVariable Long mentorId,
            jakarta.servlet.http.HttpSession session) {

        // Check whether admin is logged in
        if (session.getAttribute("adminId") == null) {
            return ResponseEntity.status(403).build();
        }

        try {

            MentorEvaluation evaluation =
                    mentorEvaluationService
                            .approveMentor(mentorId);

            if (evaluation == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(evaluation);

        } catch (IllegalStateException error) {

            return ResponseEntity.status(409).build();
        }
    }


    // ==========================================
    // GET APPROVED MENTORS
    // ==========================================

    @GetMapping("/approved")
    public ResponseEntity<List<MentorEvaluation>>
            getApprovedMentors(HttpSession session) {

        sessionAccessService.requireAdmin(session);

        return ResponseEntity.ok(
                mentorEvaluationService
                        .getApprovedMentors()
        );
    }
}