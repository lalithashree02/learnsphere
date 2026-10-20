package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Mentor;
import com.skill_learning_platform.skill_learning_platform.repository.MentorRepository;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/mentors")
@CrossOrigin(origins = "*")
public class AdminMentorVerificationController {

    private final MentorRepository mentorRepository;
    private final SessionAccessService sessionAccessService;

    public AdminMentorVerificationController(MentorRepository mentorRepository,
            SessionAccessService sessionAccessService) {
        this.mentorRepository = mentorRepository;
        this.sessionAccessService = sessionAccessService;
    }

    // Get all registered mentors
    @GetMapping
    public ResponseEntity<List<Mentor>> getAllMentors(HttpSession session) {
        sessionAccessService.requireAdmin(session);
        List<Mentor> mentors = mentorRepository.findAll();
        mentors.forEach(mentor -> mentor.setPassword(null));
        return ResponseEntity.ok(mentors);
    }

    // Get one mentor by ID
    @GetMapping("/{id}")
    public ResponseEntity<Mentor> getMentor(@PathVariable Long id, HttpSession session) {

        sessionAccessService.requireAdmin(session);

        Mentor mentor = mentorRepository.findById(id).orElse(null);

        if (mentor == null) {
            return ResponseEntity.notFound().build();
        }

        mentor.setPassword(null);

        return ResponseEntity.ok(mentor);
    }
}