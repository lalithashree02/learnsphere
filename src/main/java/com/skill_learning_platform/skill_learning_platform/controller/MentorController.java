package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Mentor;
import com.skill_learning_platform.skill_learning_platform.service.MentorService;
import com.skill_learning_platform.skill_learning_platform.repository.MentorRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import com.skill_learning_platform.skill_learning_platform.service.CourseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mentors")
@CrossOrigin(origins = "*")
public class MentorController {

    private final MentorService mentorService;
    private final MentorRepository mentorRepository;
    private final MentorEvaluationRepository evaluationRepository;
    private final SessionAccessService sessionAccessService;
    private final CourseService courseService;

    public MentorController(MentorService mentorService,
                            MentorRepository mentorRepository,
                            MentorEvaluationRepository evaluationRepository,
                            SessionAccessService sessionAccessService,
                            CourseService courseService) {
        this.mentorService = mentorService;
        this.mentorRepository = mentorRepository;
        this.evaluationRepository = evaluationRepository;
        this.sessionAccessService = sessionAccessService;
        this.courseService = courseService;
    }

    // Register a mentor
    @PostMapping("/register")
    public ResponseEntity<Mentor> registerMentor(@RequestBody Mentor mentor) {
        Mentor savedMentor = mentorService.registerMentor(mentor);
        savedMentor.setPassword(null);
        return ResponseEntity.ok(savedMentor);
    }

    // Get all mentors
    // Get all mentors
@GetMapping
public ResponseEntity<List<Mentor>> getAllMentors(HttpSession session) {

    List<Mentor> mentors;

    // Admin can see all mentors
    if (session.getAttribute("adminId") != null) {

        mentors = mentorService.getAllMentors();

    } else {

        // Student can see only approved mentors
        if (session.getAttribute("studentId") != null) {

            var approvedIds =
                    evaluationRepository
                            .findByStatusIgnoreCase("APPROVED")
                            .stream()
                            .map(evaluation -> evaluation.getMentorId())
                            .toList();

            mentors = mentorService
                    .getAllMentors()
                    .stream()
                    .filter(mentor ->
                            approvedIds.contains(mentor.getId()))
                    .toList();

        } else {

            // Mentor can see only their own profile
            Object mentorId =
                    session.getAttribute("mentorId");

            if (mentorId == null) {

                sessionAccessService.requireStudent(
                        session,
                        null
                );

                // Explicitly stop here so Java knows
                // mentorId cannot be null below.
                return ResponseEntity.status(403).build();
            }

            Mentor self =
                    mentorService.getMentorById(
                            Long.valueOf(mentorId.toString())
                    );

            if (self != null) {
                self.setPassword(null);
            }

            return ResponseEntity.ok(
                    self == null
                            ? List.of()
                            : List.of(self)
            );
        }
    }

    // Never send passwords to the frontend
    mentors.forEach(mentor ->
            mentor.setPassword(null)
    );

    return ResponseEntity.ok(mentors);
}

    @GetMapping("/skill/{skill}")
    public ResponseEntity<List<Mentor>> getMentorsBySkill(@PathVariable String skill) {
        List<Mentor> mentors = mentorService.getMentorsBySkill(skill);
        mentors.forEach(mentor -> mentor.setPassword(null));
        return ResponseEntity.ok(mentors);
    }

    // Mentor Login
    @PostMapping("/login")
    public ResponseEntity<Mentor> loginMentor(@RequestBody Mentor loginRequest, HttpSession session) {

        Mentor mentor = mentorService.loginMentor(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (mentor != null) {
            session.removeAttribute("studentId");
            session.removeAttribute("adminId");
            session.setAttribute("mentorId", mentor.getId());
            mentor.setPassword(null);
            return ResponseEntity.ok(mentor);
        }

        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutMentor(HttpSession session) {
        session.removeAttribute("mentorId");
        return ResponseEntity.noContent().build();
    }

    // Upload Mentor Profile Photo
    @PostMapping("/{id}/profile-photo")
    public ResponseEntity<String> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        sessionAccessService.requireMentor(session, id);

        try {

            // Find mentor
            Mentor mentor = mentorRepository.findById(id).orElse(null);

            if (mentor == null) {
                return ResponseEntity.notFound().build();
            }

            // Check whether a file was selected
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please select a photo.");
            }

            // Create upload folder
            Path uploadPath = Paths.get("uploads/mentors");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Get original file extension
            String originalFileName = file.getOriginalFilename();
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(
                        originalFileName.lastIndexOf(".")
                );
            }

            // Create unique file name
            String fileName = UUID.randomUUID() + extension;

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // Save file path in database
            mentor.setProfilePhoto("/uploads/mentors/" + fileName);
            mentorRepository.save(mentor);

            return ResponseEntity.ok(
                    "Profile photo uploaded successfully!"
            );

        } catch (IOException e) {

            return ResponseEntity.internalServerError()
                    .body("Error uploading profile photo.");
        }
    }
    // Update Mentor Profile
@PutMapping("/{id}")
public ResponseEntity<Mentor> updateMentor(
        @PathVariable Long id,
        @RequestBody Mentor updatedMentor,
        HttpSession session) {

    sessionAccessService.requireMentor(session, id);

    if (updatedMentor.getMentorSignature() != null
            && !updatedMentor.getMentorSignature().isBlank()
            && evaluationRepository.findByMentorId(id)
                    .filter(evaluation -> "APPROVED".equalsIgnoreCase(evaluation.getStatus()))
                    .isEmpty()) {
        return ResponseEntity.status(403).build();
    }

    Mentor mentor = mentorService.updateMentor(id, updatedMentor);

    if (mentor == null) {
        return ResponseEntity.notFound().build();
    }

    courseService.updateMentorName(id, mentor.getFullName());
    mentor.setPassword(null);

    return ResponseEntity.ok(mentor);
}

@PostMapping("/{id}/signature-file")
public ResponseEntity<Map<String, Object>> uploadMentorSignature(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file,
        HttpSession session) {

    sessionAccessService.requireMentor(session, id);

    boolean approved = evaluationRepository.findByMentorId(id)
            .filter(evaluation -> "APPROVED".equalsIgnoreCase(evaluation.getStatus()))
            .isPresent();
    if (!approved) {
        return ResponseEntity.status(403).body(Map.of("message", "Only approved mentors can save a certificate signature."));
    }

    if (file == null || file.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("message", "Please upload a signature file."));
    }

    String originalName = file.getOriginalFilename() == null ? "signature" : file.getOriginalFilename();
    String lowerName = originalName.toLowerCase();
    boolean supported = lowerName.endsWith(".pdf")
            || lowerName.endsWith(".png")
            || lowerName.endsWith(".jpg")
            || lowerName.endsWith(".jpeg")
            || lowerName.endsWith(".svg");

    if (!supported) {
        return ResponseEntity.badRequest().body(Map.of("message", "Only PDF, PNG, JPG, JPEG, and SVG signature files are allowed."));
    }

    Mentor mentor = mentorRepository.findById(id).orElse(null);
    if (mentor == null) {
        return ResponseEntity.notFound().build();
    }

    try {
        Path uploadDir = Paths.get("uploads", "mentors", String.valueOf(id));
        Files.createDirectories(uploadDir);

        String extension = lowerName.substring(lowerName.lastIndexOf('.'));
        String fileName = "signature" + extension;
        Path target = uploadDir.resolve(fileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target);
        }

        String signaturePath = "/uploads/mentors/" + id + "/" + fileName;
        mentor.setMentorSignature(signaturePath);
        Mentor saved = mentorRepository.save(mentor);
        saved.setPassword(null);
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "mentorSignature", saved.getMentorSignature()
        ));
    } catch (IOException e) {
        return ResponseEntity.internalServerError().body(Map.of("message", "Unable to save the signature file."));
    }
}

@PutMapping("/{id}/signature")
public ResponseEntity<?> saveMentorSignature(
        @PathVariable Long id,
        @RequestBody java.util.Map<String, String> payload,
        HttpSession session) {

    sessionAccessService.requireMentor(session, id);

    boolean approved = evaluationRepository.findByMentorId(id)
            .filter(evaluation -> "APPROVED".equalsIgnoreCase(evaluation.getStatus()))
            .isPresent();
    if (!approved) {
        return ResponseEntity.status(403).body("Only approved mentors can save a certificate signature.");
    }

    String signature = payload.get("signature");
    if (signature == null || signature.isBlank()) {
        return ResponseEntity.badRequest().body("A signature is required.");
    }

    Mentor mentor = mentorRepository.findById(id).orElse(null);
    if (mentor == null) {
        return ResponseEntity.notFound().build();
    }

    mentor.setMentorSignature(signature.trim());
    Mentor saved = mentorRepository.save(mentor);
    saved.setPassword(null);
    return ResponseEntity.ok(saved);
}
}