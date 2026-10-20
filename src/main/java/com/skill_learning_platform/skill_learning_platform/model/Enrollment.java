package com.skill_learning_platform.skill_learning_platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {"studentId", "courseId"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long courseId;
    private Long mentorId;
    private LocalDateTime enrollmentDate;
    private String status;

    public Enrollment() {
    }

    public Enrollment(Long studentId, Long courseId, Long mentorId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.mentorId = mentorId;
        this.enrollmentDate = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public Long getCourseId() { return courseId; }
    public Long getMentorId() { return mentorId; }
    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}