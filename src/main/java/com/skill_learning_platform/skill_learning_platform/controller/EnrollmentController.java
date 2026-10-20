package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Enrollment;
import com.skill_learning_platform.skill_learning_platform.service.EnrollmentService;
import com.skill_learning_platform.skill_learning_platform.repository.EnrollmentRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import com.skill_learning_platform.skill_learning_platform.model.Student;
import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SessionAccessService sessionAccessService;

    public EnrollmentController(EnrollmentService enrollmentService,
            EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
            CourseRepository courseRepository, SessionAccessService sessionAccessService) {
        this.enrollmentService = enrollmentService;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.sessionAccessService = sessionAccessService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getStudentEnrollments(
            @PathVariable Long studentId, HttpSession session) {
        sessionAccessService.requireStudent(session, studentId);
        return ResponseEntity.ok(enrollmentService.getActiveForStudent(studentId));
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<Map<String, Object>>> getMentorEnrollments(
            @PathVariable Long mentorId, HttpSession session) {
        sessionAccessService.requireMentor(session, mentorId);
        List<Map<String, Object>> result = enrollmentRepository
                .findByMentorIdAndStatusIn(mentorId, List.of("ACTIVE", "COMPLETED"))
                .stream().map(enrollment -> {
                    Student student = studentRepository.findById(enrollment.getStudentId()).orElse(null);
                    Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("studentId", enrollment.getStudentId());
                    row.put("studentName", student == null ? "Student" : student.getName());
                    row.put("studentEmail", student == null ? "" : student.getEmail());
                    row.put("courseId", enrollment.getCourseId());
                    row.put("courseName", course == null ? "Course" : course.getCourseName());
                    row.put("enrollmentDate", enrollment.getEnrollmentDate());
                    row.put("status", enrollment.getStatus());
                    return row;
                }).toList();
        return ResponseEntity.ok(result);
    }
}