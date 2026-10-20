package com.skill_learning_platform.skill_learning_platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_topic_progress")
public class StudentTopicProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private Long courseId;

    private Long topicId;

    private boolean completed;

    public StudentTopicProgress() {
    }

    public StudentTopicProgress(
            Long studentId,
            Long courseId,
            Long topicId,
            boolean completed) {

        this.studentId = studentId;
        this.courseId = courseId;
        this.topicId = topicId;
        this.completed = completed;
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

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}