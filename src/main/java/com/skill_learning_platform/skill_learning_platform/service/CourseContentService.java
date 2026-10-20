package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.CourseContentQuestion;
import com.skill_learning_platform.skill_learning_platform.model.CourseContentTestResult;
import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;
import com.skill_learning_platform.skill_learning_platform.repository.CourseContentQuestionRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseContentTestResultRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseTopicRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentTopicProgressRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class CourseContentService {

    private final CourseTopicRepository topicRepository;
    private final CourseContentQuestionRepository questionRepository;
    private final CourseContentTestResultRepository resultRepository;
    private final StudentTopicProgressRepository progressRepository;
    private final EnrollmentService enrollmentService;
    private final StudentTopicProgressService progressService;

    public CourseContentService(
            CourseTopicRepository topicRepository,
            CourseContentQuestionRepository questionRepository,
            CourseContentTestResultRepository resultRepository,
            StudentTopicProgressRepository progressRepository,
            EnrollmentService enrollmentService,
            StudentTopicProgressService progressService) {

        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.progressRepository = progressRepository;
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
    }

    // ============================================================
    // GET ALL CONTENT FOR A COURSE
    // ============================================================

    public List<CourseTopic> getCourseContent(Long courseId) {

        return topicRepository.findByCourseIdOrderByTopicNumberAsc(courseId);
    }

    // ============================================================
    // GET CURRENT CONTENT FOR STUDENT
    // ============================================================

    public CourseTopic getCurrentContent(Long studentId, Long courseId) {

        requireEnrollment(studentId, courseId);

        return getCourseContent(courseId)
                .stream()
                .filter(topic ->
                        !isCompleted(
                                studentId,
                                courseId,
                                topic.getId()
                        ))
                .findFirst()
                .orElse(null);
    }

    // ============================================================
    // GET ONE CONTENT
    // ============================================================

    public CourseTopic getContent(Long contentId) {

        return topicRepository.findById(contentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Content not found."
                        )
                );
    }

    // ============================================================
    // CREATE CONTENT
    // ============================================================

    public CourseTopic createContent(
            CourseTopic content,
            Long courseId) {

        if (content.getTopicTitle() == null
                || content.getTopicTitle().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content title is required."
            );
        }

        String type =
                content.getContentType() == null
                        ? "TEXT"
                        : content.getContentType()
                                .trim()
                                .toUpperCase();

        if (!type.equals("TEXT")
                && !type.equals("QUESTION_TEST")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content type must be TEXT or QUESTION_TEST."
            );
        }

        if (type.equals("TEXT")
                && (content.getContent() == null
                || content.getContent().isBlank())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Text content is required."
            );
        }

        content.setCourseId(courseId);
        content.setContentType(type);

        int nextTopicNumber =
                getCourseContent(courseId)
                        .stream()
                        .mapToInt(topic ->
                                topic.getTopicNumber())
                        .max()
                        .orElse(0) + 1;

        content.setTopicNumber(nextTopicNumber);

        return topicRepository.save(content);
    }

    // ============================================================
    // UPDATE CONTENT
    // ============================================================

    public CourseTopic updateContent(
            Long contentId,
            CourseTopic updated) {

        CourseTopic existing =
                getContent(contentId);

        if (updated.getTopicTitle() == null
                || updated.getTopicTitle().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content title is required."
            );
        }

        String type =
                updated.getContentType() == null
                        ? "TEXT"
                        : updated.getContentType()
                                .trim()
                                .toUpperCase();

        if (!type.equals("TEXT")
                && !type.equals("QUESTION_TEST")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content type must be TEXT or QUESTION_TEST."
            );
        }

        existing.setTopicTitle(
                updated.getTopicTitle()
        );

        existing.setContentType(type);

        existing.setContent(
                updated.getContent()
        );

        return topicRepository.save(existing);
    }

    // ============================================================
    // DELETE CONTENT
    // ============================================================

    @Transactional
    public void deleteContent(Long contentId) {

        if (progressRepository.existsByTopicId(contentId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Content with student progress cannot be deleted."
            );
        }

        questionRepository.deleteByContentId(contentId);

        resultRepository.deleteByContentId(contentId);

        topicRepository.deleteById(contentId);
    }

    // ============================================================
    // CHECK WHETHER STUDENT CAN READ CONTENT
    // ============================================================

    public void assertStudentCanRead(
            CourseTopic content,
            Long studentId) {

        requireEnrollment(
                studentId,
                content.getCourseId()
        );

        boolean completed =
                isCompleted(
                        studentId,
                        content.getCourseId(),
                        content.getId()
                );

        CourseTopic current =
                getCurrentContent(
                        studentId,
                        content.getCourseId()
                );

        if (!completed
                && (current == null
                || !current.getId().equals(content.getId()))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Complete course content in sequence."
            );
        }
    }

    // ============================================================
    // GET QUESTIONS FOR STUDENT
    // ============================================================

    public List<CourseContentQuestion> getQuestionsForStudent(
            Long contentId,
            Long studentId) {

        CourseTopic content =
                getContent(contentId);

        assertStudentCanRead(
                content,
                studentId
        );

        return getQuestions(contentId);
    }

    // ============================================================
    // GET ALL QUESTIONS
    // ============================================================

    public List<CourseContentQuestion> getQuestions(
            Long contentId) {

        return questionRepository
                .findByContentIdOrderByIdAsc(contentId);
    }

    // ============================================================
    // ADD QUESTION
    // ============================================================

    public CourseContentQuestion addQuestion(
            Long contentId,
            CourseContentQuestion question) {

        CourseTopic content =
                getContent(contentId);

        if (!"QUESTION_TEST".equalsIgnoreCase(
                content.getContentType())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Questions can only be added to test content."
            );
        }

        if (question.getQuestion() == null
                || question.getQuestion().isBlank()
                || blank(question.getOptionA())
                || blank(question.getOptionB())
                || blank(question.getOptionC())
                || blank(question.getOptionD())
                || question.getMarks() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Complete all question fields and use positive marks."
            );
        }

        String answer =
                question.getCorrectAnswer() == null
                        ? ""
                        : question.getCorrectAnswer()
                                .trim()
                                .toUpperCase();

        if (!List.of("A", "B", "C", "D")
                .contains(answer)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Correct answer must be A, B, C, or D."
            );
        }

        question.setCorrectAnswer(answer);

        question.setContentId(contentId);

        return questionRepository.save(question);
    }

    // ============================================================
    // SUBMIT TEST
    // ============================================================

    @Transactional
    public CourseContentTestResult submitTest(
            Long studentId,
            Long contentId,
            Map<Long, String> answers) {

        CourseTopic content =
                getContent(contentId);

        assertStudentCanRead(
                content,
                studentId
        );

        if (!"QUESTION_TEST".equalsIgnoreCase(
                content.getContentType())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This content is not a test."
            );
        }

        if (resultRepository
                .findByStudentIdAndContentId(
                        studentId,
                        contentId)
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This test has already been completed."
            );
        }

        List<CourseContentQuestion> questions =
                questionRepository
                        .findByContentIdOrderByIdAsc(
                                contentId
                        );

        if (questions.isEmpty()
                || answers == null
                || questions.stream()
                        .anyMatch(question ->
                                !answers.containsKey(
                                        question.getId()))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Answer every test question before submitting."
            );
        }

        int total =
                questions.stream()
                        .mapToInt(question ->
                                question.getMarks())
                        .sum();

        int earned =
                questions.stream()
                        .filter(question -> {

                            String submittedAnswer =
                                    answers.get(
                                            question.getId()
                                    );

                            return submittedAnswer != null
                                    && question
                                    .getCorrectAnswer()
                                    .equalsIgnoreCase(
                                            submittedAnswer
                                                    .trim()
                                    );
                        })
                        .mapToInt(question ->
                                question.getMarks())
                        .sum();

        double percentage =
                Math.round(
                        earned * 10000.0 / total
                ) / 100.0;

        CourseContentTestResult result =
                resultRepository.save(
                        new CourseContentTestResult(
                                studentId,
                                content.getCourseId(),
                                contentId,
                                total,
                                earned,
                                percentage
                        )
                );

        progressService.markTestCompleted(
                studentId,
                content.getCourseId(),
                contentId
        );

        return result;
    }

    // ============================================================
    // CHECK COMPLETION
    // ============================================================

    private boolean isCompleted(
            Long studentId,
            Long courseId,
            Long contentId) {

        return progressRepository
                .findByStudentIdAndTopicId(
                        studentId,
                        contentId
                )
                .filter(progress ->
                        progress.getCourseId()
                                .equals(courseId))
                .map(progress ->
                        progress.isCompleted())
                .orElse(false);
    }

    // ============================================================
    // CHECK ENROLLMENT
    // ============================================================

    private void requireEnrollment(
            Long studentId,
            Long courseId) {

        if (!enrollmentService.hasAccess(
                studentId,
                courseId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "An active enrollment is required to access course content."
            );
        }
    }

    // ============================================================
    // CHECK BLANK VALUE
    // ============================================================

    private boolean blank(String value) {

        return value == null || value.isBlank();
    }
}