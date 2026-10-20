package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.model.CourseMessage;
import com.skill_learning_platform.skill_learning_platform.repository.CourseMessageRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class CourseChatService {
    private final CourseMessageRepository messageRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    public CourseChatService(CourseMessageRepository messageRepository,
            CourseRepository courseRepository, EnrollmentService enrollmentService) {
        this.messageRepository = messageRepository;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
    }

    public List<CourseMessage> getMessages(Long courseId, Long studentId, Long mentorId) {
        Course course = getCourse(courseId);
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is required for a course chat.");
        }

        Long resolvedMentorId = mentorId != null ? mentorId : course.getMentorId();

        if (resolvedMentorId == null || !resolvedMentorId.equals(course.getMentorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course mentor access is required.");
        }

        requireEnrollment(studentId, courseId);
        return messageRepository.findConversation(courseId, studentId, resolvedMentorId);
    }

    public CourseMessage send(CourseMessage message) {
        Course course = getCourse(message.getCourseId());
        String role = message.getSenderRole() == null ? "" : message.getSenderRole().toUpperCase(Locale.ROOT);
        if ("STUDENT".equals(role)) {
            requireEnrollment(message.getSenderId(), message.getCourseId());
            if (!course.getMentorId().equals(message.getReceiverId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Messages must go to this course mentor.");
            }
        } else if ("MENTOR".equals(role)) {
            if (!course.getMentorId().equals(message.getSenderId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course mentor access is required.");
            }
            requireEnrollment(message.getReceiverId(), message.getCourseId());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender role must be STUDENT or MENTOR.");
        }
        if (message.getMessage() == null || message.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty.");
        }
        message.setSenderRole(role);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);
        return messageRepository.save(message);
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
    }

    private void requireEnrollment(Long studentId, Long courseId) {
        if (studentId == null || !enrollmentService.hasAccess(studentId, courseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Active enrollment is required to chat.");
        }
    }
}