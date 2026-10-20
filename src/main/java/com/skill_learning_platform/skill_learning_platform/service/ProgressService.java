package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Progress;
import com.skill_learning_platform.skill_learning_platform.repository.ProgressRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;

    public ProgressService(ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public Progress createProgress(Progress progress) {
        return progressRepository.save(progress);
    }

    public Progress getProgressById(Long id) {
        return progressRepository.findById(id).orElse(null);
    }

    public List<Progress> getStudentProgress(Long studentId) {
        return progressRepository.findByStudentId(studentId);
    }

    public Progress getCourseProgress(
            Long studentId,
            Long courseId) {

        return progressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);
    }

    public Progress updateProgress(
            Long id,
            Progress updatedProgress) {

        Progress progress =
                progressRepository.findById(id).orElse(null);

        if (progress == null) {
            return null;
        }

        progress.setCompletedTopics(
                updatedProgress.getCompletedTopics()
        );

        progress.setTotalTopics(
                updatedProgress.getTotalTopics()
        );

        progress.setProgressPercentage(
                updatedProgress.getProgressPercentage()
        );

        progress.setStatus(
                updatedProgress.getStatus()
        );

        return progressRepository.save(progress);
    }

    public boolean deleteProgress(Long id) {

        if (!progressRepository.existsById(id)) {
            return false;
        }

        progressRepository.deleteById(id);

        return true;
    }
}