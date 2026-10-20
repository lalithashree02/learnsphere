package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Certificate;
import com.skill_learning_platform.skill_learning_platform.repository.CertificateRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateSignatureService {

    private final CertificateRepository certificateRepository;


    public CertificateSignatureService(
            CertificateRepository certificateRepository) {

        this.certificateRepository =
                certificateRepository;
    }


    // ==========================================
    // GET PENDING MENTOR SIGNATURES
    // ==========================================

    public List<Certificate>
            getPendingMentorSignatures(
                    Long mentorId) {

        return certificateRepository
                .findByMentorId(mentorId)
                .stream()
                .filter(certificate ->
                        "PENDING_MENTOR_SIGNATURE"
                                .equalsIgnoreCase(
                                        certificate.getStatus()
                                )
                )
                .toList();
    }


    // ==========================================
    // MENTOR SIGNS CERTIFICATE
    // ==========================================

    public Certificate mentorSignCertificate(
            Long certificateId,
            Long mentorId,
            String signature) {

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
        // Check mentor
        // ------------------------------------------

        if (
            !certificate.getMentorId()
                       .equals(mentorId)
        ) {

            throw new IllegalStateException(
                    "You are not authorized to sign this certificate."
            );
        }


        // ------------------------------------------
        // Check certificate status
        // ------------------------------------------

        if (
            !"PENDING_MENTOR_SIGNATURE"
                    .equalsIgnoreCase(
                            certificate.getStatus()
                    )
        ) {

            throw new IllegalStateException(
                    "This certificate is not waiting for mentor signature."
            );
        }


        // ------------------------------------------
        // Check signature
        // ------------------------------------------

        if (
            signature == null
            ||
            signature.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Mentor signature is required."
            );
        }


        // ------------------------------------------
        // Save mentor signature
        // ------------------------------------------

        certificate.setMentorSignature(
                signature
        );


        // ------------------------------------------
        // Move to admin stage
        // ------------------------------------------

        certificate.setStatus(
                "PENDING_ADMIN_SIGNATURE"
        );


        return certificateRepository.save(
                certificate
        );
    }
}
