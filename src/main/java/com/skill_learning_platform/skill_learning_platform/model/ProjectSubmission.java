package com.skill_learning_platform.skill_learning_platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "project_submissions")
public class ProjectSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private Long courseId;

    private Long mentorId;

    private String githubUrl;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String mentorFeedback;

    private String submittedDate;

    private String reviewedDate;

    public ProjectSubmission() {
    }

    public ProjectSubmission(
            Long studentId,
            Long courseId,
            Long mentorId,
            String githubUrl,
            String status,
            String mentorFeedback,
            String submittedDate,
            String reviewedDate) {

        this.studentId = studentId;
        this.courseId = courseId;
        this.mentorId = mentorId;
        this.githubUrl = githubUrl;
        this.status = status;
        this.mentorFeedback = mentorFeedback;
        this.submittedDate = submittedDate;
        this.reviewedDate = reviewedDate;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMentorFeedback() {
        return mentorFeedback;
    }

    public void setMentorFeedback(String mentorFeedback) {
        this.mentorFeedback = mentorFeedback;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getReviewedDate() {
        return reviewedDate;
    }

    public void setReviewedDate(String reviewedDate) {
        this.reviewedDate = reviewedDate;
    }
}