package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.StudentRequest;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRequestRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class StudentRequestServiceTest {

    @Test
    void acceptingCourseRequestCreatesEnrollment() {
        StudentRequestRepository requestRepository = mock(StudentRequestRepository.class);
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        StudentRequest request = pendingCourseRequest();
        when(requestRepository.findById(21L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        StudentRequest result = service(requestRepository, enrollmentService)
                .updateStatus(21L, "ACCEPTED", 9L);

        assertEquals("ACCEPTED", result.getStatus());
        verify(enrollmentService).enroll(4L, 12L, 9L);
    }

    @Test
    void rejectingCourseRequestDoesNotCreateEnrollment() {
        StudentRequestRepository requestRepository = mock(StudentRequestRepository.class);
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        StudentRequest request = pendingCourseRequest();
        when(requestRepository.findById(21L)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);

        StudentRequest result = service(requestRepository, enrollmentService)
                .updateStatus(21L, "REJECTED", 9L);

        assertEquals("REJECTED", result.getStatus());
        verify(enrollmentService, never()).enroll(4L, 12L, 9L);
    }

    private StudentRequestService service(StudentRequestRepository requests,
            EnrollmentService enrollments) {
        return new StudentRequestService(requests, mock(CourseRepository.class),
                mock(StudentRepository.class), enrollments,
                mock(MentorEvaluationRepository.class));
    }

    private StudentRequest pendingCourseRequest() {
        StudentRequest request = new StudentRequest();
        request.setId(21L);
        request.setStudentId(4L);
        request.setMentorId(9L);
        request.setCourseId(12L);
        request.setStatus("PENDING");
        return request;
    }
}
