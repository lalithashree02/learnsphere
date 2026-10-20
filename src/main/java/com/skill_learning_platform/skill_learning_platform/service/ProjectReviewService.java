package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.ProjectSubmission;
import com.skill_learning_platform.skill_learning_platform.repository.ProjectSubmissionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectReviewService {

    private final ProjectSubmissionRepository
            projectSubmissionRepository;

    private final CertificateService certificateService;


    public ProjectReviewService(
            ProjectSubmissionRepository projectSubmissionRepository,
            CertificateService certificateService) {

        this.projectSubmissionRepository =
                projectSubmissionRepository;
        this.certificateService = certificateService;
    }


    // ==========================================
    // GET PENDING PROJECTS
    // ==========================================

    public List<ProjectSubmission>
            getPendingProjects(Long mentorId) {

        return projectSubmissionRepository
                .findByMentorIdAndStatus(
                        mentorId,
                        "PENDING_REVIEW"
                );
    }


    // ==========================================
    // REVIEW PROJECT
    // ==========================================

        @Transactional
        public ProjectSubmission reviewProject(
            Long submissionId,
            Long mentorId,
            String status,
            String feedback) {

        ProjectSubmission submission =
                projectSubmissionRepository
                        .findById(submissionId)
                        .orElse(null);


        if (submission == null) {

            throw new IllegalStateException(
                    "Project submission not found."
            );
        }


        // Make sure this project belongs
        // to the logged-in mentor

        if (
            !submission.getMentorId()
                       .equals(mentorId)
        ) {

            throw new IllegalStateException(
                    "You are not authorized to review this project."
            );
        }


        // Only allow these two review results

        if (
            !status.equals("APPROVED")
            &&
            !status.equals("CHANGES_REQUIRED")
        ) {

            throw new IllegalArgumentException(
                    "Invalid review status."
            );
        }


        submission.setStatus(status);

        submission.setMentorFeedback(feedback);

        submission.setReviewedDate(
                LocalDate.now().toString()
        );


        ProjectSubmission savedSubmission =
                projectSubmissionRepository.save(
                        submission
                );

        if ("APPROVED".equalsIgnoreCase(status)) {

            certificateService.createCertificate(
                    submission.getStudentId(),
                    submission.getCourseId()
            );
        }

        return savedSubmission;
    }
}