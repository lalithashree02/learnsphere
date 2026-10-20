package com.skill_learning_platform.skill_learning_platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mentor_evaluations")
public class MentorEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mentorId;

    private String githubUrl;

    private String linkedinUrl;

    @Column(columnDefinition = "TEXT")
    private String githubAnalysis;

    @Column(columnDefinition = "TEXT")
    private String linkedinAnalysis;

    private double githubScore;

    private double linkedinScore;

    private double finalScore;

    private String status;


    public MentorEvaluation() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }


    public String getGithubAnalysis() {
        return githubAnalysis;
    }

    public void setGithubAnalysis(String githubAnalysis) {
        this.githubAnalysis = githubAnalysis;
    }


    public String getLinkedinAnalysis() {
        return linkedinAnalysis;
    }

    public void setLinkedinAnalysis(String linkedinAnalysis) {
        this.linkedinAnalysis = linkedinAnalysis;
    }


    public double getGithubScore() {
        return githubScore;
    }

    public void setGithubScore(double githubScore) {
        this.githubScore = githubScore;
    }


    public double getLinkedinScore() {
        return linkedinScore;
    }

    public void setLinkedinScore(double linkedinScore) {
        this.linkedinScore = linkedinScore;
    }


    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}