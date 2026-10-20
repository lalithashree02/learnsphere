package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.MCQResult;
import com.skill_learning_platform.skill_learning_platform.service.MCQResultService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mcq/results")
@CrossOrigin(origins = "*")
public class MCQResultController {

    private final MCQResultService mcqResultService;
    private final SessionAccessService sessionAccessService;

    public MCQResultController(
            MCQResultService mcqResultService,
            SessionAccessService sessionAccessService) {
        this.mcqResultService = mcqResultService;
        this.sessionAccessService = sessionAccessService;
    }

    @PostMapping
    public ResponseEntity<MCQResult> saveResult(
            @RequestBody MCQResult result,
            HttpSession session) {

        sessionAccessService.requireMentor(session, result.getMentorId());
        return ResponseEntity.ok(
                mcqResultService.saveResult(result)
        );
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<MCQResult> getResultByMentorId(
            @PathVariable Long mentorId,
            HttpSession session) {

        if (session.getAttribute("adminId") == null) {
            sessionAccessService.requireMentor(session, mentorId);
        }

        MCQResult result =
                mcqResultService.getResultByMentorId(mentorId);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}