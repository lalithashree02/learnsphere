package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Progress;
import com.skill_learning_platform.skill_learning_platform.service.ProgressService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    private final ProgressService progressService;
    private final SessionAccessService sessionAccessService;

    public ProgressController(
            ProgressService progressService,
            SessionAccessService sessionAccessService) {
        this.progressService = progressService;
        this.sessionAccessService = sessionAccessService;
    }

    // Create progress
    @PostMapping
    public ResponseEntity<Progress> createProgress(
            @RequestBody Progress progress,
            HttpSession session) {

        sessionAccessService.requireStudent(session, progress.getStudentId());
        Progress savedProgress =
                progressService.createProgress(progress);

        return ResponseEntity.ok(savedProgress);
    }

    // Get all progress for a student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Progress>> getStudentProgress(
            @PathVariable Long studentId,
            HttpSession session) {

        sessionAccessService.requireStudent(session, studentId);
        return ResponseEntity.ok(
                progressService.getStudentProgress(studentId)
        );
    }

    // Get progress for one course
    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<Progress> getCourseProgress(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            HttpSession session) {

        sessionAccessService.requireStudent(session, studentId);
        Progress progress =
                progressService.getCourseProgress(
                        studentId,
                        courseId
                );

        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(progress);
    }

    // Update progress
    @PutMapping("/{id}")
    public ResponseEntity<Progress> updateProgress(
            @PathVariable Long id,
            @RequestBody Progress updatedProgress,
            HttpSession session) {

        Progress existingProgress = progressService.getProgressById(id);
        if (existingProgress == null) {
            return ResponseEntity.notFound().build();
        }
        sessionAccessService.requireStudent(
                session,
                existingProgress.getStudentId()
        );

        Progress progress =
                progressService.updateProgress(
                        id,
                        updatedProgress
                );

        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(progress);
    }

    // Delete progress
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgress(
            @PathVariable Long id,
            HttpSession session) {

        Progress existingProgress = progressService.getProgressById(id);
        if (existingProgress == null) {
            return ResponseEntity.notFound().build();
        }
        sessionAccessService.requireStudent(
                session,
                existingProgress.getStudentId()
        );

        boolean deleted =
                progressService.deleteProgress(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}