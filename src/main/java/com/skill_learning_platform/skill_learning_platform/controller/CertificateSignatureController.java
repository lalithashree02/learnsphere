package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Certificate;
import com.skill_learning_platform.skill_learning_platform.service.CertificateSignatureService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificate-signatures")
@CrossOrigin(origins = "*")
public class CertificateSignatureController {

    private final CertificateSignatureService
            certificateSignatureService;

        private final SessionAccessService sessionAccessService;


        public CertificateSignatureController(
                        CertificateSignatureService certificateSignatureService,
                        SessionAccessService sessionAccessService) {

        this.certificateSignatureService =
                certificateSignatureService;
                this.sessionAccessService = sessionAccessService;
    }


    // ==========================================
    // GET PENDING MENTOR SIGNATURES
    // ==========================================

    @GetMapping("/mentor/{mentorId}/pending")
    public ResponseEntity<List<Certificate>>
            getPendingMentorSignatures(
                                        @PathVariable Long mentorId,
                                        HttpSession session) {

                sessionAccessService.requireMentor(session, mentorId);

        return ResponseEntity.ok(
                certificateSignatureService
                        .getPendingMentorSignatures(
                                mentorId
                        )
        );
    }


    // ==========================================
    // MENTOR SIGNS CERTIFICATE
    // ==========================================

    @PutMapping("/{certificateId}/mentor-sign")
    public ResponseEntity<?> mentorSignCertificate(

            @PathVariable Long certificateId,

            @RequestParam Long mentorId,

                        @RequestParam String signature,
                        HttpSession session) {

                sessionAccessService.requireMentor(session, mentorId);
                return ResponseEntity.status(410)
                                .body(Map.of("message", "Certificate signatures are saved once in the mentor profile."));
    }
}