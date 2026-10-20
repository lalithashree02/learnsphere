package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.MCQQuestionResponse;
import com.skill_learning_platform.skill_learning_platform.model.MCQQuestion;
import com.skill_learning_platform.skill_learning_platform.service.MCQQuestionService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcq/questions")
@CrossOrigin(origins = "*")
public class MCQQuestionController {

    private final MCQQuestionService mcqQuestionService;
    private final SessionAccessService sessionAccessService;

    public MCQQuestionController(
            MCQQuestionService mcqQuestionService,
            SessionAccessService sessionAccessService) {
        this.mcqQuestionService = mcqQuestionService;
        this.sessionAccessService = sessionAccessService;
    }

    @PostMapping
    public ResponseEntity<MCQQuestion> addQuestion(
            @RequestBody MCQQuestion question,
            HttpSession session) {

        sessionAccessService.requireAdmin(session);
        return ResponseEntity.ok(
                mcqQuestionService.addQuestion(question)
        );
    }

    @GetMapping
    public ResponseEntity<List<MCQQuestion>> getAllQuestions(
            HttpSession session) {

        sessionAccessService.requireAdmin(session);
        return ResponseEntity.ok(
                mcqQuestionService.getAllQuestions()
        );
    }

    @GetMapping("/skill/{skill}")
public ResponseEntity<List<MCQQuestionResponse>> getQuestionsBySkill(
        @PathVariable String skill,
        HttpSession session) {

    requireAdminOrMentor(session);

    List<MCQQuestion> questions =
            mcqQuestionService.getQuestionsBySkill(skill);

    List<MCQQuestionResponse> response = questions.stream()
            .map(question -> new MCQQuestionResponse(
                    question.getId(),
                    question.getQuestion(),
                    question.getOptionA(),
                    question.getOptionB(),
                    question.getOptionC(),
                    question.getOptionD()
            ))
            .toList();

    return ResponseEntity.ok(response);
}

    @GetMapping("/{id}")
    public ResponseEntity<MCQQuestion> getQuestionById(
            @PathVariable Long id,
            HttpSession session) {

        sessionAccessService.requireAdmin(session);
        MCQQuestion question =
                mcqQuestionService.getQuestionById(id);

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
        mcqQuestionService.deleteQuestion(id);

        return ResponseEntity.ok().build();
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