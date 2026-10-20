package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.MentorEvaluation;
import com.skill_learning_platform.skill_learning_platform.model.MCQResult;
import com.skill_learning_platform.skill_learning_platform.model.ProgrammingResult;
import com.skill_learning_platform.skill_learning_platform.repository.MCQResultRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;
import com.skill_learning_platform.skill_learning_platform.repository.ProgrammingResultRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MentorEvaluationServiceTest {

    @Test
    void savingRoundThreeCalculatesAverageAcrossAllThreeRounds() {
        MentorEvaluationRepository repository =
                mock(MentorEvaluationRepository.class);
        MCQResultRepository mcqRepository = mock(MCQResultRepository.class);
        ProgrammingResultRepository programmingRepository =
                mock(ProgrammingResultRepository.class);
        MentorEvaluation evaluation = new MentorEvaluation();
        evaluation.setMentorId(7L);
        evaluation.setGithubScore(52);
        evaluation.setLinkedinScore(36);
        MCQResult mcqResult = new MCQResult();
        mcqResult.setPercentage(80);
        ProgrammingResult programmingResult = new ProgrammingResult();
        programmingResult.setPercentage(60);

        when(repository.findAllByMentorIdOrderByIdDesc(7L))
                .thenReturn(List.of());
        when(mcqRepository.findByMentorId(7L)).thenReturn(Optional.of(mcqResult));
        when(programmingRepository.findByMentorId(7L))
                .thenReturn(List.of(programmingResult));
        when(repository.save(evaluation)).thenReturn(evaluation);

        MentorEvaluation saved = new MentorEvaluationService(
                repository, mcqRepository, programmingRepository)
                .saveEvaluation(evaluation);

        assertEquals(77.2, saved.getFinalScore());
        assertEquals("PENDING_ADMIN_REVIEW", saved.getStatus());
    }

    @Test
    void savingRoundThreeIsBlockedUntilEarlierRoundsAreComplete() {
        MentorEvaluationRepository repository =
                mock(MentorEvaluationRepository.class);
        MCQResultRepository mcqRepository = mock(MCQResultRepository.class);
        ProgrammingResultRepository programmingRepository =
                mock(ProgrammingResultRepository.class);
        MentorEvaluation evaluation = new MentorEvaluation();
        evaluation.setMentorId(7L);
        evaluation.setGithubScore(52);
        evaluation.setLinkedinScore(36);
        when(mcqRepository.findByMentorId(7L)).thenReturn(Optional.empty());

        MentorEvaluationService service = new MentorEvaluationService(
                repository, mcqRepository, programmingRepository);

        assertThrows(IllegalStateException.class,
                () -> service.saveEvaluation(evaluation));
    }
}