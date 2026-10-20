package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.ProgrammingQuestion;
import com.skill_learning_platform.skill_learning_platform.model.ProgrammingResult;
import com.skill_learning_platform.skill_learning_platform.service.ProgrammingQuestionService;
import com.skill_learning_platform.skill_learning_platform.repository.ProgrammingResultRepository;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programming/questions")
@CrossOrigin(origins = "*")
public class ProgrammingQuestionController {

    private final ProgrammingQuestionService programmingQuestionService;
    private final ProgrammingResultRepository programmingResultRepository;
    private final SessionAccessService sessionAccessService;

    public ProgrammingQuestionController(
            ProgrammingQuestionService programmingQuestionService,
            ProgrammingResultRepository programmingResultRepository,
            SessionAccessService sessionAccessService) {

        this.programmingQuestionService = programmingQuestionService;
        this.programmingResultRepository = programmingResultRepository;
        this.sessionAccessService = sessionAccessService;
    }

    @PostMapping
    public ResponseEntity<ProgrammingQuestion> addQuestion(
            @RequestBody ProgrammingQuestion question,
            HttpSession session) {

        sessionAccessService.requireAdmin(session);
        return ResponseEntity.ok(
                programmingQuestionService.addQuestion(question)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProgrammingQuestion>> getAllQuestions(
            HttpSession session) {

        requireAdminOrMentor(session);
        return ResponseEntity.ok(
                programmingQuestionService.getAllQuestions()
        );
    }

    @GetMapping("/skill/{skill}")
    public ResponseEntity<List<ProgrammingQuestion>> getQuestionsBySkill(
            @PathVariable String skill,
            HttpSession session) {

        requireAdminOrMentor(session);
        return ResponseEntity.ok(
                programmingQuestionService.getQuestionsBySkill(skill)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgrammingQuestion> getQuestionById(
            @PathVariable Long id,
            HttpSession session) {

        requireAdminOrMentor(session);
        ProgrammingQuestion question =
                programmingQuestionService.getQuestionById(id);

        if (question == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(question);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            HttpSession session) {

        sessionAccessService.requireAdmin(session);
        programmingQuestionService.deleteQuestion(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/submit")
    public ResponseEntity<ProgrammingResult> submitProgrammingTest(
            @RequestBody ProgrammingResult result,
            HttpSession session) {

        sessionAccessService.requireMentor(session, result.getMentorId());
        ProgrammingResult savedResult =
                programmingResultRepository.save(result);

        return ResponseEntity.ok(savedResult);
    }

    private void requireAdminOrMentor(HttpSession session) {
        if (session.getAttribute("adminId") != null) {
            sessionAccessService.requireAdmin(session);
            return;
        }

        Object mentorId = session.getAttribute("mentorId");
        sessionAccessService.requireMentor(
                session,
                mentorId == null ? null : Long.valueOf(mentorId.toString())
        );
    }
}