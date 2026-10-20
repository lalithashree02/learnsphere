package com.skill_learning_platform.skill_learning_platform.model;

import java.util.List;

public class MCQTestSubmission {

    private Long mentorId;
    private List<MCQAnswer> answers;

    public MCQTestSubmission() {
    }

    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public List<MCQAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<MCQAnswer> answers) {
        this.answers = answers;
    }
}