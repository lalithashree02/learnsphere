package com.skill_learning_platform.skill_learning_platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "progress")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private Long courseId;

    private String courseName;

    private int completedTopics;

    private int totalTopics;

    private double progressPercentage;

    private String status;

    public Progress() {
    }

    public Progress(
            Long studentId,
            Long courseId,
            String courseName,
            int completedTopics,
            int totalTopics,
            double progressPercentage,
            String status) {

        this.studentId = studentId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.completedTopics = completedTopics;
        this.totalTopics = totalTopics;
        this.progressPercentage = progressPercentage;
        this.status = status;
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCompletedTopics() {
        return completedTopics;
    }

    public void setCompletedTopics(int completedTopics) {
        this.completedTopics = completedTopics;
    }

    public int getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(int totalTopics) {
        this.totalTopics = totalTopics;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}