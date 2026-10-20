package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Mentor;
import com.skill_learning_platform.skill_learning_platform.repository.MentorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MentorService {

    private final MentorRepository mentorRepository;
    private final PasswordEncoder passwordEncoder;

    public MentorService(
            MentorRepository mentorRepository,
            PasswordEncoder passwordEncoder) {
        this.mentorRepository = mentorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Register a new mentor
    public Mentor registerMentor(Mentor mentor) {
        mentor.setPassword(passwordEncoder.encode(mentor.getPassword()));
        return mentorRepository.save(mentor);
    }

    // Get all mentors
    public List<Mentor> getAllMentors() {
        return mentorRepository.findAll();
    }

    public List<Mentor> getMentorsBySkill(String skill) {
        return mentorRepository.findBySkillsContainingIgnoreCase(skill);
    }

    // Get mentor by ID
    public Mentor getMentorById(Long id) {
        return mentorRepository.findById(id).orElse(null);
    }

    // Mentor login
    public Mentor loginMentor(String email, String password) {

        Mentor mentor = mentorRepository.findByEmail(email).orElse(null);

        if (mentor == null || password == null || mentor.getPassword() == null) {
            return null;
        }

        String storedPassword = mentor.getPassword();
        if (passwordEncoder.matches(password, storedPassword)) {
            return mentor;
        }

        if (password.equals(storedPassword)) {
            mentor.setPassword(passwordEncoder.encode(password));
            return mentorRepository.save(mentor);
        }

        return null;
    }
    public Mentor updateMentor(Long id, Mentor updatedMentor) {

    Mentor mentor = mentorRepository.findById(id).orElse(null);

    if (mentor == null) {
        return null;
    }

    mentor.setFullName(updatedMentor.getFullName());
    mentor.setPhoneNumber(updatedMentor.getPhoneNumber());

    mentor.setEducationLevel(updatedMentor.getEducationLevel());
    mentor.setFieldOfStudy(updatedMentor.getFieldOfStudy());
    mentor.setInstitutionName(updatedMentor.getInstitutionName());
    mentor.setEducationStatus(updatedMentor.getEducationStatus());

    mentor.setYearsOfExperience(updatedMentor.getYearsOfExperience());
    mentor.setSpecialization(updatedMentor.getSpecialization());

    mentor.setSkills(updatedMentor.getSkills());

    if (updatedMentor.getMentorSignature() != null) {
        mentor.setMentorSignature(updatedMentor.getMentorSignature());
    }

    mentor.setLinkedinUrl(updatedMentor.getLinkedinUrl());
    mentor.setGithubUrl(updatedMentor.getGithubUrl());

    return mentorRepository.save(mentor);
}
}