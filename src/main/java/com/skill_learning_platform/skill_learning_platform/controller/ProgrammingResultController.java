package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.ProgrammingResult;
import com.skill_learning_platform.skill_learning_platform.repository.ProgrammingResultRepository;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programming/results")
@CrossOrigin(origins = "*")
public class ProgrammingResultController {

    private final ProgrammingResultRepository programmingResultRepository;
    private final SessionAccessService sessionAccessService;

    public ProgrammingResultController(
            ProgrammingResultRepository programmingResultRepository,
            SessionAccessService sessionAccessService) {

        this.programmingResultRepository =
                programmingResultRepository;
        this.sessionAccessService = sessionAccessService;
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<ProgrammingResult>> getProgrammingResults(
            @PathVariable Long mentorId,
            HttpSession session) {

        if (session.getAttribute("adminId") == null) {
            sessionAccessService.requireMentor(session, mentorId);
        }
        return ResponseEntity.ok(
                programmingResultRepository.findByMentorId(mentorId)
        );
    }
}