package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Certificate;
import com.skill_learning_platform.skill_learning_platform.service.CertificateService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class CertificateController {

    private final CertificateService certificateService;
        private final SessionAccessService sessionAccessService;


    public CertificateController(
            CertificateService certificateService,
            SessionAccessService sessionAccessService) {

        this.certificateService =
                certificateService;
        this.sessionAccessService = sessionAccessService;
    }


    // ==========================================
    // GET CERTIFICATE
    // ==========================================

    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<Certificate>
            getCertificate(
                    @PathVariable Long studentId,
                    @PathVariable Long courseId,
                    HttpSession session) {

        sessionAccessService.requireStudent(session, studentId);

        Certificate certificate =
                certificateService
                        .getCertificate(
                                studentId,
                                courseId
                        );

        if (certificate == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                certificate
        );
    }


    // ==========================================
    // GET ALL STUDENT CERTIFICATES
    // ==========================================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Certificate>>
            getStudentCertificates(
                                        @PathVariable Long studentId, HttpSession session) {

                sessionAccessService.requireStudent(session, studentId);

        return ResponseEntity.ok(
                certificateService
                        .getStudentCertificates(
                                studentId
                        )
        );
    }


    // ==========================================
    // GET CERTIFICATE BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Certificate>
            getCertificateById(
                    @PathVariable Long id, HttpSession session) {

        Certificate certificate =
                certificateService
                        .getCertificateById(id);

        if (certificate == null
                || !"ISSUED".equalsIgnoreCase(certificate.getStatus())) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

                if (session.getAttribute("adminId") == null) {
                        sessionAccessService.requireStudent(session, certificate.getStudentId());
                }

        return ResponseEntity.ok(
                certificate
        );
    }
}