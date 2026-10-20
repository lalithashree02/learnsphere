package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.StudentRequest;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRequestRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;
import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.model.Student;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class StudentRequestService {

    private final StudentRequestRepository studentRequestRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentService enrollmentService;
    private final MentorEvaluationRepository evaluationRepository;

    public StudentRequestService(
            StudentRequestRepository studentRequestRepository,
            CourseRepository courseRepository,
            StudentRepository studentRepository,
            EnrollmentService enrollmentService,
            MentorEvaluationRepository evaluationRepository) {

        this.studentRequestRepository = studentRequestRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.enrollmentService = enrollmentService;
        this.evaluationRepository = evaluationRepository;
    }

    // Create a new student request
    public StudentRequest createRequest(StudentRequest request) {
        if (request.getStudentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is required.");
        }
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
        request.setStudentName(student.getName());
        request.setStudentEmail(student.getEmail());
        request.setRequestDate(java.time.LocalDateTime.now());
        request.setStatus("PENDING");

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
            request.setMentorId(course.getMentorId());
            request.setCourseName(course.getCourseName());
            request.setSkill(course.getSkill());
            if (studentRequestRepository.existsByStudentIdAndCourseId(
                    request.getStudentId(), request.getCourseId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already requested this course.");
            }
        } else if (request.getMentorId() != null && studentRequestRepository.existsByStudentIdAndMentorId(
                request.getStudentId(), request.getMentorId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already sent a request to this mentor."
            );
        }

        if (request.getMentorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mentor is required.");
        }
        boolean approvedMentor = evaluationRepository.findByStatusIgnoreCase("APPROVED").stream()
            .anyMatch(evaluation -> request.getMentorId().equals(evaluation.getMentorId()));
        if (!approvedMentor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Requests are available only for mentors approved by an admin.");
        }

        return studentRequestRepository.save(request);
    }

    // Get all student requests
    public List<StudentRequest> getAllRequests() {
        return studentRequestRepository.findAll();
    }

    // Get requests for a particular mentor
    public List<StudentRequest> getRequestsByMentor(Long mentorId) {
        return studentRequestRepository.findByMentorId(mentorId);
    }

    public List<StudentRequest> getRequestsByStudent(Long studentId) {
        return studentRequestRepository.findByStudentIdOrderByIdDesc(studentId);
    }

    public Optional<StudentRequest> getRequest(Long studentId, Long mentorId) {
        return studentRequestRepository.findFirstByStudentIdAndMentorIdOrderByIdDesc(
                studentId, mentorId);
    }

    // Get pending requests for a particular mentor
    public List<StudentRequest> getPendingRequests(Long mentorId) {
        return studentRequestRepository
                .findByMentorIdAndStatus(mentorId, "PENDING");
    }

    // Update request status
    @Transactional
    public StudentRequest updateStatus(Long id, String status, Long mentorId) {

        StudentRequest request =
                studentRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return null;
        }

        if (request.getMentorId() == null || !request.getMentorId().equals(mentorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the assigned mentor can decide this request.");
        }

        String finalStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!finalStatus.equals("ACCEPTED") && !finalStatus.equals("REJECTED")) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Status must be ACCEPTED or REJECTED."
            );
        }

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This request has already received a final decision."
            );
        }

        request.setStatus(finalStatus);

        if ("ACCEPTED".equals(finalStatus) && request.getCourseId() != null) {
            enrollmentService.enroll(request.getStudentId(), request.getCourseId(), request.getMentorId());
        }

        return studentRequestRepository.save(request);
    }
}