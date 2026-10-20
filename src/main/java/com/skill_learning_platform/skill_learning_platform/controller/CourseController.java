package com.skill_learning_platform.skill_learning_platform.controller;

import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.service.CourseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.skill_learning_platform.skill_learning_platform.service.EnrollmentService;
import com.skill_learning_platform.skill_learning_platform.service.SessionAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final SessionAccessService sessionAccessService;

    public CourseController(CourseService courseService, EnrollmentService enrollmentService,
            SessionAccessService sessionAccessService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.sessionAccessService = sessionAccessService;
    }

    // Create a new course
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @RequestBody Course course, HttpSession session) {

        sessionAccessService.requireMentor(session, course.getMentorId());

        Course savedCourse =
                courseService.createCourse(course);

        return ResponseEntity.ok(savedCourse);
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses(HttpSession session) {

        Object studentId = session.getAttribute("studentId");
        sessionAccessService.requireStudent(session,
                studentId instanceof Long id ? id : null);

        return ResponseEntity.ok(
                courseService.getAllCourses()
        );
    }

    // Get one course
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(
            @PathVariable Long id, HttpSession session) {

        Course course =
                courseService.getCourseById(id);

        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        requireCourseReadAccess(course, session);

        return ResponseEntity.ok(course);
    }

    @GetMapping("/{id}/project-instructions")
    public ResponseEntity<Map<String, String>> getProjectInstructions(
            @PathVariable Long id, @RequestParam Long studentId, HttpSession session) {
        sessionAccessService.requireStudent(session, studentId);
        if (!enrollmentService.hasAccess(studentId, id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "An active enrollment is required to view project instructions.");
        }
        String instructions = courseService.getProjectInstructions(id);
        if (instructions == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("projectInstructions", instructions));
    }

    // Get courses by mentor
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<Course>> getCoursesByMentor(
            @PathVariable Long mentorId, HttpSession session) {

        Object signedInMentor = session.getAttribute("mentorId");
        boolean owner = signedInMentor != null && signedInMentor.toString().equals(mentorId.toString());
        boolean admin = session.getAttribute("adminId") != null;
        if (!owner && !admin) {
            Object studentId = session.getAttribute("studentId");
            sessionAccessService.requireStudent(session,
                studentId instanceof Long id ? id : null);
            boolean approved = courseService.getAllCourses().stream()
                .anyMatch(course -> mentorId.equals(course.getMentorId()));
            if (!approved) return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                courseService.getCoursesByMentor(mentorId)
        );
    }

    // Get courses by skill
    @GetMapping("/skill/{skill}")
    public ResponseEntity<List<Course>> getCoursesBySkill(
            @PathVariable String skill, HttpSession session) {

        Object studentId = session.getAttribute("studentId");
        sessionAccessService.requireStudent(session,
            studentId instanceof Long id ? id : null);

        return ResponseEntity.ok(
                courseService.getCoursesBySkill(skill)
        );
    }

    // Update a course
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course updatedCourse,
            @RequestParam Long mentorId,
            HttpSession session) {

        sessionAccessService.requireMentor(session, mentorId);
        boolean ownsCourse = courseService.getCoursesByMentor(mentorId).stream()
            .anyMatch(course -> course.getId().equals(id));
        if (!ownsCourse || !mentorId.equals(updatedCourse.getMentorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Only the course mentor can update this course.");
        }

        Course course =
                courseService.updateCourse(
                        id,
                        updatedCourse
                );

        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(course);
    }

    // Delete a course
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id,
            @RequestParam Long mentorId,
            HttpSession session) {

        sessionAccessService.requireMentor(session, mentorId);
        boolean ownsCourse = courseService.getCoursesByMentor(mentorId).stream()
            .anyMatch(course -> course.getId().equals(id));
        if (!ownsCourse) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Only the course mentor can delete this course.");
        }

        boolean deleted =
                courseService.deleteCourse(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private void requireCourseReadAccess(Course course, HttpSession session) {
        Object mentorId = session.getAttribute("mentorId");
        if (mentorId != null && course.getMentorId() != null
            && mentorId.toString().equals(course.getMentorId().toString())) return;
        if (session.getAttribute("adminId") != null) return;
        Object studentId = session.getAttribute("studentId");
        sessionAccessService.requireStudent(session,
                studentId instanceof Long id ? id : null);
        boolean approved = courseService.getAllCourses().stream()
                .anyMatch(approvedCourse -> approvedCourse.getId().equals(course.getId()));
        if (!approved) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found.");
    }
}