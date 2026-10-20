package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Student;
import com.skill_learning_platform.skill_learning_platform.repository.StudentRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Student registerStudent(Student student) {

        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        String encryptedPassword =
                passwordEncoder.encode(student.getPassword());

        student.setPassword(encryptedPassword);

        return studentRepository.save(student);
    }

    public Student validateLogin(String email, String password) {

        Student student = studentRepository.findByEmail(email);

        if (student == null) {
            return null;
        }

        String storedPassword = student.getPassword();
        if (password == null || storedPassword == null) {
            return null;
        }

        if (passwordEncoder.matches(password, storedPassword)) {
            return student;
        }

        if (password.equals(storedPassword)) {
            student.setPassword(passwordEncoder.encode(password));
            studentRepository.save(student);
            return student;
        }

        return null;
    }

    public Student getStudentById(Long id) {

        return studentRepository.findById(id).orElse(null);
    }

    public Student updateStudent(Long id, Student updatedStudent) {

        Student student =
                studentRepository.findById(id).orElse(null);

        if (student == null) {
            return null;
        }

        student.setName(updatedStudent.getName());
        student.setCareerGoal(updatedStudent.getCareerGoal());

        return studentRepository.save(student);
    }
}