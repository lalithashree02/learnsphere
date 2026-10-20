package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.StudentRequest;
import com.skill_learning_platform.skill_learning_platform.service.StudentRequestService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-requests")
@CrossOrigin(origins = "*")
public class StudentRequestController {

    private final StudentRequestService studentRequestService;
    private final SessionAccessService sessionAccessService;

    public StudentRequestController(
            StudentRequestService studentRequestService,
            SessionAccessService sessionAccessService) {

        this.studentRequestService = studentRequestService;
        this.sessionAccessService = sessionAccessService;
    }

    // Create a new student request
    @PostMapping
    public ResponseEntity<StudentRequest> createRequest(
            @RequestBody StudentRequest request, HttpSession session) {

        sessionAccessService.requireStudent(session, request.getStudentId());

        // New requests should start as PENDING
        request.setStatus("PENDING");

        StudentRequest savedRequest =
                studentRequestService.createRequest(request);

        return ResponseEntity.ok(savedRequest);
    }

    // Get all requests
    @GetMapping
    public ResponseEntity<List<StudentRequest>> getAllRequests(HttpSession session) {

        sessionAccessService.requireAdmin(session);

        return ResponseEntity.ok(
                studentRequestService.getAllRequests()
        );
    }

    // Get requests for a particular mentor
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<StudentRequest>> getRequestsByMentor(
            @PathVariable Long mentorId, HttpSession session) {

        sessionAccessService.requireMentor(session, mentorId);

        return ResponseEntity.ok(
                studentRequestService.getRequestsByMentor(mentorId)
        );
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentRequest>> getRequestsByStudent(
            @PathVariable Long studentId, HttpSession session) {
        sessionAccessService.requireStudent(session, studentId);
        return ResponseEntity.ok(studentRequestService.getRequestsByStudent(studentId));
    }

        @GetMapping("/student/{studentId}/mentor/{mentorId}")
        public ResponseEntity<StudentRequest> getRequest(
            @PathVariable Long studentId,
            @PathVariable Long mentorId,
            HttpSession session) {

        sessionAccessService.requireStudent(session, studentId);

        return studentRequestService.getRequest(studentId, mentorId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
        }

    // Get pending requests for a particular mentor
    @GetMapping("/mentor/{mentorId}/pending")
    public ResponseEntity<List<StudentRequest>> getPendingRequests(
            @PathVariable Long mentorId, HttpSession session) {

        sessionAccessService.requireMentor(session, mentorId);

        return ResponseEntity.ok(
                studentRequestService.getPendingRequests(mentorId)
        );
    }

    // Update request status
    @PutMapping("/{id}/status")
    public ResponseEntity<StudentRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam Long mentorId,
            HttpSession session) {

        sessionAccessService.requireMentor(session, mentorId);

        StudentRequest updatedRequest =
                studentRequestService.updateStatus(id, status, mentorId);

        if (updatedRequest == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedRequest);
    }
}