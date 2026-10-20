package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.MCQQuestion;
import com.skill_learning_platform.skill_learning_platform.repository.MCQQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MCQQuestionService {

    private final MCQQuestionRepository mcqQuestionRepository;

    public MCQQuestionService(MCQQuestionRepository mcqQuestionRepository) {
        this.mcqQuestionRepository = mcqQuestionRepository;
    }

    public MCQQuestion addQuestion(MCQQuestion question) {
        return mcqQuestionRepository.save(question);
    }

    public List<MCQQuestion> getAllQuestions() {
        return mcqQuestionRepository.findAll();
    }

    public List<MCQQuestion> getQuestionsBySkill(String skill) {
        return mcqQuestionRepository.findBySkill(skill);
    }

    public MCQQuestion getQuestionById(Long id) {
        return mcqQuestionRepository.findById(id).orElse(null);
    }

    public void deleteQuestion(Long id) {
        mcqQuestionRepository.deleteById(id);
    }
}