package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.CourseMessage;
import com.skill_learning_platform.skill_learning_platform.service.CourseChatService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/course-chat")
@CrossOrigin(origins = "*")
public class CourseChatController {
    private final CourseChatService chatService;
    private final SessionAccessService sessionAccessService;

    public CourseChatController(CourseChatService chatService,
            SessionAccessService sessionAccessService) {
        this.chatService = chatService;
        this.sessionAccessService = sessionAccessService;
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseMessage>> getMessages(@PathVariable Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long mentorId,
            HttpSession session) {
        if (mentorId == null) sessionAccessService.requireStudent(session, studentId);
        else sessionAccessService.requireMentor(session, mentorId);
        return ResponseEntity.ok(chatService.getMessages(courseId, studentId, mentorId));
    }

    @PostMapping
    public ResponseEntity<CourseMessage> send(@RequestBody CourseMessage message, HttpSession session) {
        if ("STUDENT".equalsIgnoreCase(message.getSenderRole())) {
            sessionAccessService.requireStudent(session, message.getSenderId());
        } else {
            sessionAccessService.requireMentor(session, message.getSenderId());
        }
        return ResponseEntity.ok(chatService.send(message));
    }
}