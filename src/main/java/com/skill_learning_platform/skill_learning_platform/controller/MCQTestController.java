package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.MCQResult;
import com.skill_learning_platform.skill_learning_platform.model.MCQTestSubmission;
import com.skill_learning_platform.skill_learning_platform.service.MCQTestService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mcq/test")
@CrossOrigin(origins = "*")
public class MCQTestController {

    private final MCQTestService mcqTestService;
    private final SessionAccessService sessionAccessService;

    public MCQTestController(
            MCQTestService mcqTestService,
            SessionAccessService sessionAccessService) {
        this.mcqTestService = mcqTestService;
        this.sessionAccessService = sessionAccessService;
    }

    @PostMapping("/submit")
    public ResponseEntity<MCQResult> submitTest(
            @RequestBody MCQTestSubmission submission,
            HttpSession session) {

        sessionAccessService.requireMentor(session, submission.getMentorId());
        MCQResult result = mcqTestService.evaluateTest(submission);

        return ResponseEntity.ok(result);
    }
}