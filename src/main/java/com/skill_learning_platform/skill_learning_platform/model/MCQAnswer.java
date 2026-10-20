package com.skill_learning_platform.skill_learning_platform.model;

public class MCQAnswer {

    private Long questionId;
    private String answer;

    public MCQAnswer() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}