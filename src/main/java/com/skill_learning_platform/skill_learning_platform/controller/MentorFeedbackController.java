package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.MentorFeedback;
import com.skill_learning_platform.skill_learning_platform.service.MentorFeedbackService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mentor-feedback")
@CrossOrigin(origins = "*")
public class MentorFeedbackController {

    private final MentorFeedbackService feedbackService;
    private final SessionAccessService sessionAccessService;

    public MentorFeedbackController(MentorFeedbackService feedbackService,
            SessionAccessService sessionAccessService) {
        this.feedbackService = feedbackService;
        this.sessionAccessService = sessionAccessService;
    }

    @PostMapping
    public ResponseEntity<MentorFeedback> submit(
            @RequestBody MentorFeedback feedback, HttpSession session) {
        sessionAccessService.requireStudent(session, feedback.getStudentId());
        return ResponseEntity.ok(feedbackService.submit(feedback));
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<MentorFeedback>> getMentorFeedback(
            @PathVariable Long mentorId, HttpSession session) {
        sessionAccessService.requireMentor(session, mentorId);
        return ResponseEntity.ok(feedbackService.getMentorFeedback(mentorId));
    }

    @GetMapping("/quality/{mentorId}")
    public ResponseEntity<Map<String, Object>> getMentorQuality(
            @PathVariable Long mentorId, HttpSession session) {
        sessionAccessService.requireSignedIn(session);
        return ResponseEntity.ok(feedbackService.getMentorQuality(mentorId));
    }

    @GetMapping("/admin/quality")
    public ResponseEntity<List<Map<String, Object>>> getAllQuality(HttpSession session) {
        sessionAccessService.requireAdmin(session);
        return ResponseEntity.ok(feedbackService.getAllQuality());
    }
}