package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.CourseContentQuestion;
import com.skill_learning_platform.skill_learning_platform.model.CourseContentTestResult;
import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;
import com.skill_learning_platform.skill_learning_platform.repository.CourseContentQuestionRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseContentTestResultRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseTopicRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentTopicProgressRepository;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseContentServiceTest {

    @Test
    void submittingTestScoresAndPersistsResultWithoutReturningAnswerKey() {

        CourseTopicRepository topics =
                mock(CourseTopicRepository.class);

        CourseContentQuestionRepository questions =
                mock(CourseContentQuestionRepository.class);

        CourseContentTestResultRepository results =
                mock(CourseContentTestResultRepository.class);

        StudentTopicProgressRepository progress =
                mock(StudentTopicProgressRepository.class);

        EnrollmentService enrollments =
                mock(EnrollmentService.class);

        StudentTopicProgressService progressService =
                mock(StudentTopicProgressService.class);

        CourseTopic test =
                mock(CourseTopic.class);

        CourseContentQuestion question =
                mock(CourseContentQuestion.class);

        when(test.getId()).thenReturn(31L);
        when(test.getCourseId()).thenReturn(12L);
        when(test.getContentType()).thenReturn("QUESTION_TEST");

        when(topics.findById(31L))
                .thenReturn(Optional.of(test));

        when(topics.findByCourseIdOrderByTopicNumberAsc(12L))
                .thenReturn(List.of(test));

        when(progress.findByStudentIdAndTopicId(4L, 31L))
                .thenReturn(Optional.empty());

        when(enrollments.hasAccess(4L, 12L))
                .thenReturn(true);

        when(question.getId())
                .thenReturn(44L);

        when(question.getMarks())
                .thenReturn(2);

        when(question.getCorrectAnswer())
                .thenReturn("B");

        when(questions.findByContentIdOrderByIdAsc(31L))
                .thenReturn(List.of(question));

        when(results.save(any(CourseContentTestResult.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CourseContentService service =
                new CourseContentService(
                        topics,
                        questions,
                        results,
                        progress,
                        enrollments,
                        progressService
                );

        CourseContentTestResult result =
                service.submitTest(
                        4L,
                        31L,
                        Map.of(44L, "b")
                );

        assertEquals(
                2,
                result.getTotalMarks()
        );

        assertEquals(
                2,
                result.getMarksObtained()
        );

        assertEquals(
                100.0,
                result.getPercentage()
        );

        verify(progressService)
                .markTestCompleted(
                        4L,
                        12L,
                        31L
                );

        verify(results)
                .save(any(CourseContentTestResult.class));
    }

    @Test
    void creatingContentAllowsNullTopicNumberFromClientAndAssignsNextSequence() {

        CourseTopicRepository topics =
                mock(CourseTopicRepository.class);

        CourseContentQuestionRepository questions =
                mock(CourseContentQuestionRepository.class);

        CourseContentTestResultRepository results =
                mock(CourseContentTestResultRepository.class);

        StudentTopicProgressRepository progress =
                mock(StudentTopicProgressRepository.class);

        EnrollmentService enrollments =
                mock(EnrollmentService.class);

        StudentTopicProgressService progressService =
                mock(StudentTopicProgressService.class);

        CourseTopic content = new CourseTopic();
        content.setCourseId(12L);
        content.setTopicTitle("New lesson");
        content.setContentType("TEXT");
        content.setContent("Lesson body");
        content.setTopicNumber(null);

        when(topics.findByCourseIdOrderByTopicNumberAsc(12L))
                .thenReturn(List.of());

        when(topics.save(any(CourseTopic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseContentService service =
                new CourseContentService(
                        topics,
                        questions,
                        results,
                        progress,
                        enrollments,
                        progressService
                );

        CourseTopic saved =
                service.createContent(content, 12L);

        assertEquals(1, saved.getTopicNumber());
        assertEquals(12L, saved.getCourseId());
        assertEquals("TEXT", saved.getContentType());
    }

    @Test
    void futureContentCannotBeCompletedOutOfSequence() {

        CourseTopicRepository topics =
                mock(CourseTopicRepository.class);

        StudentTopicProgressRepository progress =
                mock(StudentTopicProgressRepository.class);

        EnrollmentService enrollments =
                mock(EnrollmentService.class);

        CourseTopic first =
                mock(CourseTopic.class);

        CourseTopic future =
                mock(CourseTopic.class);

        when(future.getId())
                .thenReturn(2L);

        when(future.getCourseId())
                .thenReturn(12L);

        when(future.getContentType())
                .thenReturn("QUESTION_TEST");

        when(first.getId())
                .thenReturn(1L);

        when(first.getCourseId())
                .thenReturn(12L);

        when(first.getContentType())
                .thenReturn("TEXT");

        when(topics.findById(2L))
                .thenReturn(Optional.of(future));

        when(topics.findById(1L))
                .thenReturn(Optional.of(first));

        when(topics.findByCourseIdOrderByTopicNumberAsc(12L))
                .thenReturn(List.of(first, future));

        when(progress.findByStudentIdAndTopicId(4L, 1L))
                .thenReturn(Optional.empty());

        StudentTopicProgressService service =
                new StudentTopicProgressService(
                        progress,
                        topics,
                        enrollments
                );

        var exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> service.markTestCompleted(
                                4L,
                                12L,
                                2L
                        )
                );

        assertEquals(
                403,
                exception.getStatusCode().value()
        );
    }
}