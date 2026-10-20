package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Admin;
import com.skill_learning_platform.skill_learning_platform.model.Mentor;
import com.skill_learning_platform.skill_learning_platform.model.Student;
import com.skill_learning_platform.skill_learning_platform.repository.AdminRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordHandlingServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void studentLegacyPasswordIsUpgradedAfterSuccessfulLogin() {
        StudentRepository repository = mock(StudentRepository.class);
        Student student = new Student();
        student.setEmail("student@example.test");
        student.setPassword("old-password");
        when(repository.findByEmail(student.getEmail())).thenReturn(student);
        when(repository.save(student)).thenReturn(student);

        StudentService service = new StudentService(repository, passwordEncoder);
        Student loggedIn = service.validateLogin(student.getEmail(), "old-password");

        assertSame(student, loggedIn);
        assertTrue(passwordEncoder.matches("old-password", student.getPassword()));
        verify(repository).save(student);
    }

    @Test
    void mentorRegistrationEncodesPasswordAndLegacyPasswordIsUpgraded() {
        MentorRepository repository = mock(MentorRepository.class);
        Mentor mentor = new Mentor();
        mentor.setEmail("mentor@example.test");
        mentor.setPassword("new-password");
        when(repository.save(mentor)).thenReturn(mentor);

        MentorService service = new MentorService(repository, passwordEncoder);
        Mentor registered = service.registerMentor(mentor);

        assertSame(mentor, registered);
        assertTrue(passwordEncoder.matches("new-password", registered.getPassword()));

        mentor.setPassword("old-password");
        when(repository.findByEmail(mentor.getEmail())).thenReturn(Optional.of(mentor));
        when(repository.save(mentor)).thenReturn(mentor);
        Mentor loggedIn = service.loginMentor(mentor.getEmail(), "old-password");

        assertSame(mentor, loggedIn);
        assertTrue(passwordEncoder.matches("old-password", mentor.getPassword()));
        verify(repository, times(2)).save(mentor);
    }

    @Test
    void adminLegacyPasswordIsUpgradedAfterSuccessfulLogin() {
        AdminRepository repository = mock(AdminRepository.class);
        Admin admin = new Admin();
        admin.setEmail("admin@example.test");
        admin.setPassword("old-password");
        when(repository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(repository.save(admin)).thenReturn(admin);

        AdminService service = new AdminService(repository, passwordEncoder);
        Admin loggedIn = service.loginAdmin(admin.getEmail(), "old-password");

        assertNotNull(loggedIn);
        assertTrue(passwordEncoder.matches("old-password", admin.getPassword()));
        verify(repository).save(admin);
    }
}
