package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Admin;
import com.skill_learning_platform.skill_learning_platform.model.Certificate;
import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;
import com.skill_learning_platform.skill_learning_platform.model.Mentor;
import com.skill_learning_platform.skill_learning_platform.model.ProjectSubmission;
import com.skill_learning_platform.skill_learning_platform.model.Student;
import com.skill_learning_platform.skill_learning_platform.model.StudentTopicProgress;

import com.skill_learning_platform.skill_learning_platform.repository.AdminRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CertificateRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseTopicRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorRepository;
import com.skill_learning_platform.skill_learning_platform.repository.ProjectSubmissionRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentTopicProgressRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseTopicRepository courseTopicRepository;
    private final StudentTopicProgressRepository progressRepository;
    private final ProjectSubmissionRepository projectSubmissionRepository;
    private final MentorRepository mentorRepository;
    private final MentorEvaluationRepository mentorEvaluationRepository;
    private final EnrollmentService enrollmentService;

    public CertificateService(
            CertificateRepository certificateRepository,
            AdminRepository adminRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            CourseTopicRepository courseTopicRepository,
            StudentTopicProgressRepository progressRepository,
            ProjectSubmissionRepository projectSubmissionRepository,
            MentorRepository mentorRepository,
            MentorEvaluationRepository mentorEvaluationRepository,
            EnrollmentService enrollmentService) {

        this.certificateRepository = certificateRepository;
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseTopicRepository = courseTopicRepository;
        this.progressRepository = progressRepository;
        this.projectSubmissionRepository = projectSubmissionRepository;
        this.mentorRepository = mentorRepository;
        this.mentorEvaluationRepository = mentorEvaluationRepository;
        this.enrollmentService = enrollmentService;
    }

    // ==========================================================
    // CREATE CERTIFICATE AFTER MENTOR APPROVES PROJECT
    // ==========================================================

    public Certificate createCertificate(
            Long studentId,
            Long courseId) {

        // ------------------------------------------------------
        // CHECK ENROLLMENT
        // ------------------------------------------------------

        if (!enrollmentService.hasAccess(studentId, courseId)) {
            throw new IllegalStateException(
                    "An accepted course enrollment is required for a certificate."
            );
        }

        // ------------------------------------------------------
        // CHECK STUDENT
        // ------------------------------------------------------

        Student student = studentRepository
                .findById(studentId)
                .orElse(null);

        if (student == null) {
            throw new IllegalStateException(
                    "Student not found."
            );
        }

        // ------------------------------------------------------
        // CHECK COURSE
        // ------------------------------------------------------

        Course course = courseRepository
                .findById(courseId)
                .orElse(null);

        if (course == null) {
            throw new IllegalStateException(
                    "Course not found."
            );
        }

        // ------------------------------------------------------
        // CHECK ALL TOPICS COMPLETED
        // ------------------------------------------------------

        List<CourseTopic> topics =
                courseTopicRepository
                        .findByCourseIdOrderByTopicNumberAsc(courseId);

        if (topics.isEmpty()) {
            throw new IllegalStateException(
                    "This course does not have any topics."
            );
        }

        List<StudentTopicProgress> progressList =
                progressRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        );

        int completedTopics = 0;

        for (CourseTopic topic : topics) {

            for (StudentTopicProgress progress : progressList) {

                if (progress.getTopicId() != null
                        && progress.getTopicId().equals(topic.getId())
                        && progress.isCompleted()) {

                    completedTopics++;
                    break;
                }
            }
        }

        if (completedTopics < topics.size()) {
            throw new IllegalStateException(
                    "Complete all course topics before generating the certificate."
            );
        }

        // ------------------------------------------------------
        // CHECK PROJECT APPROVAL
        // ------------------------------------------------------

        ProjectSubmission projectSubmission =
                projectSubmissionRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElse(null);

        if (projectSubmission == null) {
            throw new IllegalStateException(
                    "Submit the final project before generating the certificate."
            );
        }

        if (!"APPROVED".equalsIgnoreCase(
                projectSubmission.getStatus())) {

            throw new IllegalStateException(
                    "The final project must be approved by the mentor before generating the certificate."
            );
        }

        // ------------------------------------------------------
        // CHECK COURSE MENTOR
        // ------------------------------------------------------

        if (course.getMentorId() == null
                || projectSubmission.getMentorId() == null
                || !course.getMentorId()
                        .equals(projectSubmission.getMentorId())) {

            throw new IllegalStateException(
                    "The approved project mentor does not match the course mentor."
            );
        }

        // ------------------------------------------------------
        // CHECK MENTOR
        // ------------------------------------------------------

        Mentor mentor = mentorRepository
                .findById(course.getMentorId())
                .orElse(null);

        if (mentor == null) {
            throw new IllegalStateException(
                    "Mentor not found."
            );
        }

        boolean approvedMentor =
                mentorEvaluationRepository
                        .findByMentorId(course.getMentorId())
                        .filter(evaluation ->
                                "APPROVED".equalsIgnoreCase(
                                        evaluation.getStatus()
                                )
                        )
                        .isPresent();

        if (!approvedMentor) {
            throw new IllegalStateException(
                    "The course mentor must be approved by an admin."
            );
        }

        // ------------------------------------------------------
        // CHECK MENTOR SIGNATURE
        // ------------------------------------------------------

        if (!hasUsableSignatureReference(
                mentor.getMentorSignature())) {

            throw new IllegalStateException(
                    "The mentor must upload a signature file before approving projects for certificates."
            );
        }

        // ------------------------------------------------------
        // CHECK EXISTING CERTIFICATE
        // ------------------------------------------------------

        Certificate existingCertificate =
                certificateRepository
                        .findByStudentIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElse(null);

        if (existingCertificate != null) {

            // Already officially issued
            if ("ISSUED".equalsIgnoreCase(
                    existingCertificate.getStatus())) {

                return existingCertificate;
            }

            // Reuse and issue any previously pending certificate
            existingCertificate.setMentorSignature(
                    mentor.getMentorSignature()
            );
            return signAutomatically(existingCertificate);
        }

        // ------------------------------------------------------
        // CREATE NEW CERTIFICATE
        // ------------------------------------------------------

        String certificateNumber =
                "SLP-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        Certificate certificate = new Certificate();

        certificate.setStudentId(studentId);
        certificate.setCourseId(courseId);
        certificate.setMentorId(course.getMentorId());

        certificate.setCertificateNumber(
                certificateNumber
        );

        certificate.setStudentName(
                student.getName()
        );

        certificate.setCourseName(
                course.getCourseName()
        );

        certificate.setSkill(
                course.getSkill()
        );

        certificate.setMentorName(
                course.getMentorName()
        );

        // Use the mentor's already saved signature
        certificate.setMentorSignature(
                mentor.getMentorSignature()
        );

        return signAutomatically(certificate);
    }

    // ==========================================================
    // ADMIN APPROVES AND SIGNS CERTIFICATE
    // ==========================================================

    public Certificate approveCertificateByAdmin(
            Long certificateId) {

        Certificate certificate =
                certificateRepository
                        .findById(certificateId)
                        .orElse(null);

        if (certificate == null) {
            throw new IllegalStateException(
                    "Certificate not found."
            );
        }

        if (!"PENDING_ADMIN_SIGNATURE".equalsIgnoreCase(
                certificate.getStatus())) {

            throw new IllegalStateException(
                    "This certificate is not waiting for admin approval."
            );
        }

        return signAutomatically(certificate);
    }

    private Certificate signAutomatically(Certificate certificate) {
        Admin admin = adminRepository.findAll()
                .stream()
                .filter(candidate -> candidate.getId() != null
                        && hasUsableSignatureReference(candidate.getAdminSignature()))
                .min((first, second) -> first.getId().compareTo(second.getId()))
                .orElse(null);

        if (admin == null) {
            throw new IllegalStateException(
                    "Admin signature has not been uploaded as a valid signature file. Save it on the Admin Dashboard before approving projects."
            );
        }

        certificate.setAdminSignature(admin.getAdminSignature());
        certificate.setIssuedDate(LocalDate.now().toString());
        certificate.setStatus("ISSUED");
        return certificateRepository.save(certificate);
    }

    private boolean hasUsableSignatureReference(String signature) {
        if (signature == null) {
            return false;
        }

        String normalized = signature.trim();

        if (normalized.isEmpty()) {
            return false;
        }

        String lower = normalized.toLowerCase();
        return lower.endsWith(".pdf")
                || lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".svg")
                || lower.startsWith("http://")
                || lower.startsWith("https://")
                || normalized.contains("/")
                || normalized.contains("\\");
    }

    // ==========================================================
    // GET CERTIFICATE BY STUDENT AND COURSE
    // ==========================================================

    public Certificate getCertificate(
            Long studentId,
            Long courseId) {

                recoverCertificatesForApprovedProjects();

        return certificateRepository
                .findByStudentIdAndCourseId(
                        studentId,
                        courseId
                )
                .orElse(null);
    }

    // ==========================================================
    // GET ALL ISSUED CERTIFICATES FOR STUDENT
    // ==========================================================

    public List<Certificate> getStudentCertificates(
            Long studentId) {

                recoverCertificatesForApprovedProjects();

        return certificateRepository
                .findByStudentId(studentId)
                .stream()
                .filter(certificate ->
                        "ISSUED".equalsIgnoreCase(
                                certificate.getStatus()
                        )
                )
                .toList();
    }

    // ==========================================================
    // RECOVER CERTIFICATES FOR ALREADY APPROVED PROJECTS
    // ==========================================================

    public void recoverCertificatesForApprovedProjects() {

        for (ProjectSubmission submission :
                projectSubmissionRepository.findAll()) {

            if (!"APPROVED".equalsIgnoreCase(
                    submission.getStatus())) {

                continue;
            }

            Certificate existing =
                    certificateRepository
                            .findByStudentIdAndCourseId(
                                    submission.getStudentId(),
                                    submission.getCourseId()
                            )
                            .orElse(null);

            // Already issued
            if (existing != null
                    && "ISSUED".equalsIgnoreCase(
                            existing.getStatus())) {

                continue;
            }

            try {

                createCertificate(
                        submission.getStudentId(),
                        submission.getCourseId()
                );

            } catch (IllegalStateException ignored) {

                // Project remains without an issued certificate
                // until all certificate requirements are satisfied.
            }
        }
    }

    // ==========================================================
    // GET CERTIFICATE BY ID
    // ==========================================================

    public Certificate getCertificateById(
            Long id) {

        return certificateRepository
                .findById(id)
                .orElse(null);
    }
}