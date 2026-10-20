package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.MentorEvaluation;
import com.skill_learning_platform.skill_learning_platform.model.MCQResult;
import com.skill_learning_platform.skill_learning_platform.model.ProgrammingResult;

import com.skill_learning_platform.skill_learning_platform.repository.MCQResultRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;
import com.skill_learning_platform.skill_learning_platform.repository.ProgrammingResultRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MentorEvaluationService {

    private final MentorEvaluationRepository mentorEvaluationRepository;
    private final MCQResultRepository mcqResultRepository;
    private final ProgrammingResultRepository programmingResultRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MentorEvaluationService(
            MentorEvaluationRepository mentorEvaluationRepository,
            MCQResultRepository mcqResultRepository,
            ProgrammingResultRepository programmingResultRepository) {

        this.mentorEvaluationRepository =
                mentorEvaluationRepository;

        this.mcqResultRepository =
                mcqResultRepository;

        this.programmingResultRepository =
                programmingResultRepository;
    }


    // =========================================================
    // GET LATEST EVALUATION OF A MENTOR
    // =========================================================

    public MentorEvaluation getEvaluationByMentorId(
            Long mentorId) {

        return mentorEvaluationRepository
                .findAllByMentorIdOrderByIdDesc(mentorId)
                .stream()
                .findFirst()
                .orElse(null);
    }


    // =========================================================
    // SAVE ROUND 3 AND CALCULATE FINAL SCORE
    // =========================================================

    public MentorEvaluation saveEvaluation(
            MentorEvaluation evaluation) {


        if (evaluation.getMentorId() == null) {

            throw new IllegalArgumentException(
                    "mentorId is required"
            );
        }

        Long mentorId = evaluation.getMentorId();

        validateRoundThreeScores(evaluation);

        MCQResult mcqResult =
                mcqResultRepository
                        .findByMentorId(mentorId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Complete Round 1 before saving the final score."
                                )
                        );

        List<ProgrammingResult> programmingResults =
                programmingResultRepository
                        .findByMentorId(mentorId);

        if (programmingResults.isEmpty()) {

            throw new IllegalStateException(
                    "Complete Round 2 before saving the final score."
            );
        }

        ProgrammingResult programmingResult =
                programmingResults.get(
                        programmingResults.size() - 1
                );

        double mcqScore =
                (mcqResult.getPercentage() * 30.0) / 100.0;

        double programmingScore =
                (programmingResult.getPercentage() * 30.0) / 100.0;

        double roundThreeScore =
                evaluation.getGithubScore()
                + evaluation.getLinkedinScore();

        double roundThreeWeightedScore =
                (roundThreeScore * 40.0) / 100.0;

        double finalScore =
                mcqScore
                + programmingScore
                + roundThreeWeightedScore;
        finalScore =
                Math.round(finalScore * 100.0) / 100.0;

        evaluation.setFinalScore(finalScore);
        evaluation.setStatus(
                "PENDING_ADMIN_REVIEW"
        );


        // -----------------------------------------------------
        // Update existing evaluation instead of creating
        // duplicate records
        // -----------------------------------------------------

        MentorEvaluation existing =
                getEvaluationByMentorId(mentorId);

        if (existing != null) {

            evaluation.setId(
                    existing.getId()
            );
        }


        // -----------------------------------------------------
        // Save evaluation
        // -----------------------------------------------------

        return mentorEvaluationRepository.save(
                evaluation
        );
    }


    // =========================================================
    // GET APPROVED MENTORS
    // =========================================================

    public List<MentorEvaluation> getApprovedMentors() {

        return mentorEvaluationRepository
                .findByStatusIgnoreCase("APPROVED");
    }


    // =========================================================
    // ADMIN APPROVES MENTOR
    // =========================================================

    public MentorEvaluation approveMentor(
            Long mentorId) {

        MentorEvaluation evaluation =
                getEvaluationByMentorId(mentorId);


        // -----------------------------------------------------
        // No evaluation found
        // -----------------------------------------------------

        if (evaluation == null) {

            return null;
        }


        // -----------------------------------------------------
        // Check Round 1
        // -----------------------------------------------------

        boolean round1Completed =
                mcqResultRepository
                        .findByMentorId(mentorId)
                        .isPresent();


        // -----------------------------------------------------
        // Check Round 2
        // -----------------------------------------------------

        boolean round2Completed =
                !programmingResultRepository
                        .findByMentorId(mentorId)
                        .isEmpty();


        // -----------------------------------------------------
        // Check Round 3
        //
        // If the status is PENDING_ADMIN_REVIEW,
        // Round 3 was already submitted through saveEvaluation().
        // -----------------------------------------------------

        boolean round3Completed =
                evaluation.getStatus() != null
                && evaluation.getStatus()
                        .equalsIgnoreCase(
                                "PENDING_ADMIN_REVIEW"
                        );


        // -----------------------------------------------------
        // Check all three rounds
        // -----------------------------------------------------

        if (!round1Completed
                || !round2Completed
                || !round3Completed) {

            throw new IllegalStateException(
                    "All three evaluation rounds must be completed before approval."
            );
        }


        // -----------------------------------------------------
        // ADMIN APPROVES
        // -----------------------------------------------------

        evaluation.setStatus(
                "APPROVED"
        );


        return mentorEvaluationRepository.save(
                evaluation
        );
    }


    // =========================================================
    // VALIDATE ROUND 3 SCORES
    // =========================================================

    private void validateRoundThreeScores(
            MentorEvaluation evaluation) {

        // GitHub maximum = 60
        // LinkedIn maximum = 40

        if (evaluation.getGithubScore() < 0
                || evaluation.getGithubScore() > 60
                || evaluation.getLinkedinScore() < 0
                || evaluation.getLinkedinScore() > 40) {

            throw new IllegalArgumentException(
                    "Round 3 scores must be within their allowed ranges."
            );
        }
    }
}