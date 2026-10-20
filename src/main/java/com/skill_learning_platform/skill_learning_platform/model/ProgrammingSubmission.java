package com.skill_learning_platform.skill_learning_platform.model;

public class ProgrammingSubmission {

    private Long mentorId;
    private Long questionId;
    private String code;

    public ProgrammingSubmission() {}

    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}