package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Certificate;
import com.skill_learning_platform.skill_learning_platform.model.Admin;
import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.model.Mentor;
import com.skill_learning_platform.skill_learning_platform.model.ProjectSubmission;
import com.skill_learning_platform.skill_learning_platform.model.MentorEvaluation;
import com.skill_learning_platform.skill_learning_platform.model.Student;
import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;
import com.skill_learning_platform.skill_learning_platform.model.StudentTopicProgress;
import com.skill_learning_platform.skill_learning_platform.repository.CertificateRepository;
import com.skill_learning_platform.skill_learning_platform.repository.AdminRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseTopicRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;
import com.skill_learning_platform.skill_learning_platform.repository.ProjectSubmissionRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentTopicProgressRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CertificateServiceTest {

    @Test
    void creatingCertificateAfterProjectApprovalUsesStoredMentorAndAdminSignaturesAndIssuesIt() {
        CertificateRepository certificateRepository = mock(CertificateRepository.class);
        AdminRepository adminRepository = mock(AdminRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        CourseTopicRepository courseTopicRepository = mock(CourseTopicRepository.class);
        StudentTopicProgressRepository progressRepository = mock(StudentTopicProgressRepository.class);
        ProjectSubmissionRepository projectSubmissionRepository = mock(ProjectSubmissionRepository.class);
        MentorRepository mentorRepository = mock(MentorRepository.class);
        MentorEvaluationRepository mentorEvaluationRepository = mock(MentorEvaluationRepository.class);
        EnrollmentService enrollmentService = mock(EnrollmentService.class);

        Student student = new Student("Asha", "asha@example.test", "password", "Java");

        Course course = new Course();
        course.setMentorId(33L);
        course.setCourseName("Spring Boot");
        course.setSkill("Java");
        course.setMentorName("Mr. Mentor");

        Mentor mentor = new Mentor();
        mentor.setId(33L);
        mentor.setFullName("Mr. Mentor");
        mentor.setMentorSignature("uploads/mentors/mentor-signature.pdf");

        MentorEvaluation evaluation = new MentorEvaluation();
        evaluation.setMentorId(33L);
        evaluation.setStatus("APPROVED");

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setAdminSignature("uploads/admins/admin-signature.pdf");

        CourseTopic topic = mock(CourseTopic.class);
        when(topic.getId()).thenReturn(5L);

        ProjectSubmission project = new ProjectSubmission();
        project.setStudentId(11L);
        project.setCourseId(22L);
        project.setMentorId(33L);
        project.setStatus("APPROVED");

        when(enrollmentService.hasAccess(11L, 22L)).thenReturn(true);
        when(studentRepository.findById(11L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(22L)).thenReturn(Optional.of(course));
        when(courseTopicRepository.findByCourseIdOrderByTopicNumberAsc(22L)).thenReturn(List.of(topic));
        when(progressRepository.findByStudentIdAndCourseId(11L, 22L))
            .thenReturn(List.of(new StudentTopicProgress(11L, 22L, 5L, true)));
        when(projectSubmissionRepository.findByStudentIdAndCourseId(11L, 22L)).thenReturn(Optional.of(project));
        when(mentorRepository.findById(33L)).thenReturn(Optional.of(mentor));
        when(adminRepository.findAll()).thenReturn(List.of(admin));
        when(mentorEvaluationRepository.findByMentorId(33L)).thenReturn(Optional.of(evaluation));
        when(certificateRepository.findByStudentIdAndCourseId(11L, 22L)).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CertificateService service = new CertificateService(
                certificateRepository,
            adminRepository,
                studentRepository,
                courseRepository,
                courseTopicRepository,
                progressRepository,
                projectSubmissionRepository,
                mentorRepository,
                mentorEvaluationRepository,
                enrollmentService
        );

        Certificate certificate = service.createCertificate(11L, 22L);

        assertEquals("ISSUED", certificate.getStatus());
        assertEquals("uploads/mentors/mentor-signature.pdf", certificate.getMentorSignature());
        assertEquals("uploads/admins/admin-signature.pdf", certificate.getAdminSignature());
        assertEquals("Asha", certificate.getStudentName());
        assertEquals("Spring Boot", certificate.getCourseName());
    }
}
