package com.skill_learning_platform.skill_learning_platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;

    private String skill;

    private Long mentorId;

    private String mentorName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String projectInstructions;

    private String status;

    public Course() {
    }

    public Course(
            String courseName,
            String skill,
            Long mentorId,
            String mentorName,
            String description,
            String projectInstructions,
            String status) {

        this.courseName = courseName;
        this.skill = skill;
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.description = description;
        this.projectInstructions = projectInstructions;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectInstructions() {
        return projectInstructions;
    }

    public void setProjectInstructions(String projectInstructions) {
        this.projectInstructions = projectInstructions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}