package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.MCQResult;
import com.skill_learning_platform.skill_learning_platform.repository.MCQResultRepository;
import org.springframework.stereotype.Service;

@Service
public class MCQResultService {

    private final MCQResultRepository mcqResultRepository;

    public MCQResultService(MCQResultRepository mcqResultRepository) {
        this.mcqResultRepository = mcqResultRepository;
    }

    public MCQResult saveResult(MCQResult result) {
        return mcqResultRepository.save(result);
    }

    public MCQResult getResultByMentorId(Long mentorId) {
        return mcqResultRepository.findByMentorId(mentorId).orElse(null);
    }
}