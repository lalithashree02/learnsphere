package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.MCQAnswer;
import com.skill_learning_platform.skill_learning_platform.model.MCQQuestion;
import com.skill_learning_platform.skill_learning_platform.model.MCQResult;
import com.skill_learning_platform.skill_learning_platform.model.MCQTestSubmission;
import com.skill_learning_platform.skill_learning_platform.repository.MCQQuestionRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MCQResultRepository;

import org.springframework.stereotype.Service;

@Service
public class MCQTestService {

    private final MCQQuestionRepository mcqQuestionRepository;
    private final MCQResultRepository mcqResultRepository;

    public MCQTestService(MCQQuestionRepository mcqQuestionRepository,
                           MCQResultRepository mcqResultRepository) {
        this.mcqQuestionRepository = mcqQuestionRepository;
        this.mcqResultRepository = mcqResultRepository;
    }

    public MCQResult evaluateTest(MCQTestSubmission submission) {

        int score = 0;
        int totalQuestions = submission.getAnswers().size();

        for (MCQAnswer answer : submission.getAnswers()) {

            MCQQuestion question =
                    mcqQuestionRepository.findById(answer.getQuestionId()).orElse(null);

            if (question != null &&
                question.getCorrectAnswer().equals(answer.getAnswer())) {

                score++;
            }
        }

        double percentage = 0;

        if (totalQuestions > 0) {
            percentage = ((double) score / totalQuestions) * 100;
        }

        MCQResult result =
                mcqResultRepository.findByMentorId(submission.getMentorId())
                        .orElse(new MCQResult());

        result.setMentorId(submission.getMentorId());
        result.setScore(score);
        result.setTotalQuestions(totalQuestions);
        result.setPercentage(percentage);

        return mcqResultRepository.save(result);
    }
}