package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Mentor;
import com.skill_learning_platform.skill_learning_platform.model.MentorEvaluation;
import com.skill_learning_platform.skill_learning_platform.service.MentorEvaluationService;
import com.skill_learning_platform.skill_learning_platform.service.MentorService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/student-mentors/approved")
@CrossOrigin(origins = "*")
public class StudentMentorController {

    private final MentorEvaluationService mentorEvaluationService;
    private final MentorService mentorService;
    private final SessionAccessService sessionAccessService;

    public StudentMentorController(
            MentorEvaluationService mentorEvaluationService,
            MentorService mentorService,
            SessionAccessService sessionAccessService) {

        this.mentorEvaluationService = mentorEvaluationService;
        this.mentorService = mentorService;
        this.sessionAccessService = sessionAccessService;
    }

    // Get all approved mentors
    @GetMapping
    public ResponseEntity<List<Mentor>> getApprovedMentors(
            HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        sessionAccessService.requireStudent(
                session,
                studentId instanceof Long id ? id : null
        );

        List<MentorEvaluation> approvedEvaluations =
                mentorEvaluationService.getApprovedMentors();

        List<Mentor> approvedMentors =
                new ArrayList<>();

        for (MentorEvaluation evaluation : approvedEvaluations) {

            Mentor mentor =
                    mentorService.getMentorById(
                            evaluation.getMentorId()
                    );

            if (mentor != null) {

                // Do not send mentor password
                mentor.setPassword(null);

                approvedMentors.add(mentor);
            }
        }

        return ResponseEntity.ok(approvedMentors);
    }

    // Get one approved mentor
    @GetMapping("/{mentorId}")
    public ResponseEntity<Mentor> getApprovedMentor(
            @PathVariable Long mentorId,
            HttpSession session) {

        Object studentId = session.getAttribute("studentId");

        sessionAccessService.requireStudent(
                session,
                studentId instanceof Long id ? id : null
        );

        boolean approved =
                mentorEvaluationService
                        .getApprovedMentors()
                        .stream()
                        .anyMatch(
                                evaluation ->
                                        mentorId.equals(
                                                evaluation.getMentorId()
                                        )
                        );

        if (!approved) {
            return ResponseEntity.notFound().build();
        }

        Mentor mentor =
                mentorService.getMentorById(mentorId);

        if (mentor == null) {
            return ResponseEntity.notFound().build();
        }

        // Do not send mentor password
        mentor.setPassword(null);

        return ResponseEntity.ok(mentor);
    }
}