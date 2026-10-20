package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.ProgrammingQuestion;
import com.skill_learning_platform.skill_learning_platform.repository.ProgrammingQuestionRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgrammingQuestionService {

    private final ProgrammingQuestionRepository programmingQuestionRepository;

    public ProgrammingQuestionService(
            ProgrammingQuestionRepository programmingQuestionRepository) {

        this.programmingQuestionRepository = programmingQuestionRepository;
    }

    public ProgrammingQuestion addQuestion(ProgrammingQuestion question) {
        return programmingQuestionRepository.save(question);
    }

    public List<ProgrammingQuestion> getAllQuestions() {
        return programmingQuestionRepository.findAll();
    }

    public List<ProgrammingQuestion> getQuestionsBySkill(String skill) {
        return programmingQuestionRepository.findBySkill(skill);
    }

    public ProgrammingQuestion getQuestionById(Long id) {
        return programmingQuestionRepository.findById(id).orElse(null);
    }

    public void deleteQuestion(Long id) {
        programmingQuestionRepository.deleteById(id);
    }
}