package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Student;
import com.skill_learning_platform.skill_learning_platform.service.StudentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    private final SessionAccessService sessionAccessService;

    public StudentController(StudentService studentService, SessionAccessService sessionAccessService) {
        this.studentService = studentService;
        this.sessionAccessService = sessionAccessService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(
            @RequestBody Student student) {

        try {

            Student registeredStudent =
                    studentService.registerStudent(student);

            // Do not send encrypted password back
            registeredStudent.setPassword(null);

            return new ResponseEntity<>(
                    registeredStudent,
                    HttpStatus.CREATED
            );

        } catch (RuntimeException e) {

            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(
            @RequestBody Student student,
            HttpSession session) {

        Student loggedInStudent =
                studentService.validateLogin(
                        student.getEmail(),
                        student.getPassword()
                );

        if (loggedInStudent != null) {

            session.removeAttribute("mentorId");
            session.removeAttribute("adminId");
            session.setAttribute("studentId", loggedInStudent.getId());

            // Do not send password to frontend
            loggedInStudent.setPassword(null);

            return ResponseEntity.ok(loggedInStudent);
        }

        return new ResponseEntity<>(
                "Invalid email or password!",
                HttpStatus.UNAUTHORIZED
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutStudent(HttpSession session) {
        session.removeAttribute("studentId");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(
            @PathVariable Long id, HttpSession session) {

        if (session.getAttribute("adminId") == null) {
            sessionAccessService.requireStudent(session, id);
        }

        Student student =
                studentService.getStudentById(id);

        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        student.setPassword(null);

        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody Student updatedStudent,
            HttpSession session) {

        sessionAccessService.requireStudent(session, id);

        Student student =
                studentService.updateStudent(
                        id,
                        updatedStudent
                );

        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        student.setPassword(null);

        return ResponseEntity.ok(student);
    }
}