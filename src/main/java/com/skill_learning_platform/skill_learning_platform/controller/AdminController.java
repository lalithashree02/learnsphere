package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Admin;
import com.skill_learning_platform.skill_learning_platform.repository.AdminRepository;
import com.skill_learning_platform.skill_learning_platform.service.AdminService;
import com.skill_learning_platform.skill_learning_platform.service.CertificateService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final SessionAccessService sessionAccessService;
    private final CertificateService certificateService;

    public AdminController(AdminService adminService,
                          AdminRepository adminRepository,
                          SessionAccessService sessionAccessService,
                          CertificateService certificateService) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
        this.sessionAccessService = sessionAccessService;
        this.certificateService = certificateService;
    }

    @PostMapping("/login")
    public ResponseEntity<Admin> loginAdmin(
            @RequestBody Admin loginRequest,
            HttpSession session) {

        Admin admin =
                adminService.loginAdmin(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                );

        if (admin != null) {
            session.removeAttribute("studentId");
            session.removeAttribute("mentorId");
            session.setAttribute("adminId", admin.getId());
            admin.setPassword(null);
            return ResponseEntity.ok(admin);
        }

        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutAdmin(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/signature-file")
    public ResponseEntity<Map<String, Object>> uploadAdminSignature(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        sessionAccessService.requireAdmin(session);

        if (!id.equals(Long.valueOf(session.getAttribute("adminId").toString()))) {
            return ResponseEntity.status(403).build();
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

        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path uploadDir = Paths.get("uploads", "admins", String.valueOf(id));
            Files.createDirectories(uploadDir);

            String extension = lowerName.substring(lowerName.lastIndexOf('.'));
            String fileName = "signature" + extension;
            Path target = uploadDir.resolve(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String signaturePath = "/uploads/admins/" + id + "/" + fileName;
            admin.setAdminSignature(signaturePath);
            adminRepository.save(admin);
            certificateService.recoverCertificatesForApprovedProjects();
            return ResponseEntity.ok(Map.of(
                    "id", admin.getId(),
                    "adminSignature", admin.getAdminSignature()
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Unable to save the signature file."));
        }
    }

    @PutMapping("/{id}/signature")
    public ResponseEntity<Map<String, Object>> saveAdminSignature(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        sessionAccessService.requireAdmin(session);

        if (!id.equals(Long.valueOf(session.getAttribute("adminId").toString()))) {
            return ResponseEntity.status(403).build();
        }

        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }

        String signature = payload.get("signature");
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        admin.setAdminSignature(signature.trim());
        adminRepository.save(admin);
        certificateService.recoverCertificatesForApprovedProjects();
        return ResponseEntity.ok(Map.of(
            "id", admin.getId(),
            "adminSignature", admin.getAdminSignature()
        ));
    }
}