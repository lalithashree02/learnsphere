package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Certificate;
import com.skill_learning_platform.skill_learning_platform.model.Admin;
import com.skill_learning_platform.skill_learning_platform.repository.AdminRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CertificateRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminCertificateSignatureService {

    private final CertificateRepository certificateRepository;
        private final AdminRepository adminRepository;
                private final CertificateService certificateService;


    public AdminCertificateSignatureService(
                        CertificateRepository certificateRepository,
                        AdminRepository adminRepository,
                        CertificateService certificateService) {

        this.certificateRepository =
                certificateRepository;
                this.adminRepository = adminRepository;
                this.certificateService = certificateService;
    }


    // ==========================================
    // GET PENDING ADMIN SIGNATURES
    // ==========================================

    public List<Certificate>
            getPendingAdminSignatures() {

                certificateService.recoverCertificatesForApprovedProjects();

        return certificateRepository
                .findAll()
                .stream()
                .filter(certificate ->
                        "PENDING_ADMIN_SIGNATURE"
                                .equalsIgnoreCase(
                                        certificate.getStatus()
                                )
                )
                .toList();
    }


    // ==========================================
    // ADMIN SIGNS CERTIFICATE
    // ==========================================

        public Certificate adminSignCertificate(
                        Long adminId,
                        Long certificateId) {

                Admin admin = adminRepository.findById(adminId).orElse(null);

                if (admin == null) {
                        throw new IllegalStateException("Admin not found.");
                }

        Certificate certificate =
                certificateRepository
                        .findById(certificateId)
                        .orElse(null);


        if (certificate == null) {

            throw new IllegalStateException(
                    "Certificate not found."
            );
        }


        // ------------------------------------------
        // CHECK CERTIFICATE STATUS
        // ------------------------------------------

        if (
            !"PENDING_ADMIN_SIGNATURE"
                    .equalsIgnoreCase(
                            certificate.getStatus()
                    )
        ) {

            throw new IllegalStateException(
                    "This certificate is not waiting for admin signature."
            );
        }


        // ------------------------------------------
        // CHECK SIGNATURE
        // ------------------------------------------

        String finalSignature = admin.getAdminSignature();

        if (finalSignature == null || finalSignature.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Admin signature is required. Save one in the Admin profile first."
            );
        }


        // ------------------------------------------
        // SAVE ADMIN SIGNATURE
        // ------------------------------------------

        certificate.setAdminSignature(
                finalSignature
        );

        certificate.setIssuedDate(
                LocalDate.now().toString()
        );


        // ------------------------------------------
        // FINAL STATUS
        // ------------------------------------------

        certificate.setStatus(
                "ISSUED"
        );


        return certificateRepository.save(
                certificate
        );
    }
}