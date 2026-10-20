package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.model.CourseMessage;
import com.skill_learning_platform.skill_learning_platform.repository.CourseMessageRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseChatServiceTest {

    @Test
    void getMessagesFiltersToTheSpecificStudentMentorConversation() {
        CourseMessageRepository messageRepository = mock(CourseMessageRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentService enrollmentService = mock(EnrollmentService.class);

        Course course = new Course();
        course.setMentorId(7L);

        CourseMessage message = new CourseMessage();
        message.setCourseId(99L);
        message.setSenderId(5L);
        message.setReceiverId(7L);
        message.setMessage("Hi mentor");

        when(courseRepository.findById(99L)).thenReturn(Optional.of(course));
        when(enrollmentService.hasAccess(5L, 99L)).thenReturn(true);
        when(messageRepository.findConversation(99L, 5L, 7L)).thenReturn(List.of(message));

        CourseChatService service = new CourseChatService(messageRepository, courseRepository, enrollmentService);

        List<CourseMessage> result = service.getMessages(99L, 5L, 7L);

        assertEquals(1, result.size());
        assertEquals("Hi mentor", result.get(0).getMessage());
        verify(messageRepository).findConversation(99L, 5L, 7L);
    }
}
