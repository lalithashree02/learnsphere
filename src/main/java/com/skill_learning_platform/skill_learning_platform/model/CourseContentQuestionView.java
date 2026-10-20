package com.skill_learning_platform.skill_learning_platform.model;

public record CourseContentQuestionView(
        Long id,
        String question,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        int marks) {

    public static CourseContentQuestionView from(CourseContentQuestion question) {
        return new CourseContentQuestionView(question.getId(), question.getQuestion(),
                question.getOptionA(), question.getOptionB(), question.getOptionC(),
                question.getOptionD(), question.getMarks());
    }
}