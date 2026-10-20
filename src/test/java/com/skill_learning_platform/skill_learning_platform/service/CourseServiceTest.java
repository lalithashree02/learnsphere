package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseServiceTest {

    @Test
    void shouldUpdateMentorNameOnAllMentorCourses() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        MentorEvaluationRepository evaluationRepository = mock(MentorEvaluationRepository.class);
        CourseService courseService = new CourseService(courseRepository, evaluationRepository);
        Course firstCourse = new Course();
        Course secondCourse = new Course();
        List<Course> courses = List.of(firstCourse, secondCourse);
        when(courseRepository.findByMentorId(7L)).thenReturn(courses);

        courseService.updateMentorName(7L, "Updated Mentor");

        assertEquals("Updated Mentor", firstCourse.getMentorName());
        assertEquals("Updated Mentor", secondCourse.getMentorName());
        verify(courseRepository).saveAll(courses);
    }
}
