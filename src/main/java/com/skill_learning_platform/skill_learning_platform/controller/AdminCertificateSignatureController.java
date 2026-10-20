package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Certificate;
import com.skill_learning_platform.skill_learning_platform.service.AdminCertificateSignatureService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin-certificate-signatures")
@CrossOrigin(origins = "*")
public class AdminCertificateSignatureController {

    private final AdminCertificateSignatureService
            adminCertificateSignatureService;
        private final SessionAccessService sessionAccessService;


    public AdminCertificateSignatureController(
            AdminCertificateSignatureService
                    adminCertificateSignatureService,
            SessionAccessService sessionAccessService) {

        this.adminCertificateSignatureService =
                adminCertificateSignatureService;
        this.sessionAccessService = sessionAccessService;
    }


    // ==========================================
    // GET PENDING ADMIN SIGNATURES
    // ==========================================

    @GetMapping("/pending")
    public ResponseEntity<List<Certificate>>
                        getPendingAdminSignatures(HttpSession session) {

                sessionAccessService.requireAdmin(session);

        return ResponseEntity.ok(
                adminCertificateSignatureService
                        .getPendingAdminSignatures()
        );
    }


    // ==========================================
    // ADMIN SIGNS CERTIFICATE
    // ==========================================

    @PutMapping("/{certificateId}/sign")
    public ResponseEntity<?> adminSignCertificate(

            @PathVariable Long certificateId,

                        HttpSession session) {

                sessionAccessService.requireAdmin(session);

        try {

            Long adminId =
                    Long.valueOf(
                            session.getAttribute("adminId")
                                    .toString()
                    );

            Certificate certificate =
                    adminCertificateSignatureService
                            .adminSignCertificate(
                                    adminId,
                                    certificateId
                            );

            return ResponseEntity.ok(
                    certificate
            );

        } catch (IllegalArgumentException error) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", error.getMessage()));

        } catch (IllegalStateException error) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", error.getMessage()));
        }
    }
}
