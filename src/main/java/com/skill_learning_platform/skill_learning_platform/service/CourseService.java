package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import com.skill_learning_platform.skill_learning_platform.repository.MentorEvaluationRepository;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final MentorEvaluationRepository evaluationRepository;

    public CourseService(
            CourseRepository courseRepository,
            MentorEvaluationRepository evaluationRepository) {

        this.courseRepository = courseRepository;
        this.evaluationRepository = evaluationRepository;
    }

    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {

        Set<Long> approvedMentors =
                approvedMentorIds();

        return courseRepository.findAll()
                .stream()
                .filter(course ->
                        approvedMentors.contains(
                                course.getMentorId()
                        )
                )
                .map(course ->
                        hideProjectInstructions(course)
                )
                .toList();
    }

    public Course getCourseById(Long id) {

        return courseRepository.findById(id)
                .map(course ->
                        hideProjectInstructions(course)
                )
                .orElse(null);
    }

    public List<Course> getCoursesByMentor(
            Long mentorId) {

        return courseRepository
                .findByMentorId(mentorId)
                .stream()
                .map(course ->
                        hideProjectInstructions(course)
                )
                .toList();
    }

    public void updateMentorName(Long mentorId, String mentorName) {
        List<Course> courses = courseRepository.findByMentorId(mentorId);
        courses.forEach(course -> course.setMentorName(mentorName));
        courseRepository.saveAll(courses);
    }

    public List<Course> getCoursesBySkill(
            String skill) {

        Set<Long> approvedMentors =
                approvedMentorIds();

        return courseRepository
                .findBySkillIgnoreCase(skill)
                .stream()
                .filter(course ->
                        approvedMentors.contains(
                                course.getMentorId()
                        )
                )
                .map(course ->
                        hideProjectInstructions(course)
                )
                .toList();
    }

    public String getProjectInstructions(
            Long courseId) {

        return courseRepository.findById(courseId)
                .map(course ->
                        course.getProjectInstructions()
                )
                .orElse(null);
    }

    public Course updateCourse(
            Long id,
            Course updatedCourse) {

        Course course =
                courseRepository
                        .findById(id)
                        .orElse(null);

        if (course == null) {
            return null;
        }

        course.setCourseName(
                updatedCourse.getCourseName()
        );

        course.setSkill(
                updatedCourse.getSkill()
        );

        course.setMentorId(
                updatedCourse.getMentorId()
        );

        course.setMentorName(
                updatedCourse.getMentorName()
        );

        course.setDescription(
                updatedCourse.getDescription()
        );

        course.setProjectInstructions(
                updatedCourse.getProjectInstructions()
        );

        course.setStatus(
                updatedCourse.getStatus()
        );

        return courseRepository.save(course);
    }

    public boolean deleteCourse(Long id) {

        if (!courseRepository.existsById(id)) {
            return false;
        }

        courseRepository.deleteById(id);

        return true;
    }

    private Course hideProjectInstructions(
            Course course) {

        course.setProjectInstructions(null);

        return course;
    }

    private Set<Long> approvedMentorIds() {

        return evaluationRepository
                .findByStatusIgnoreCase("APPROVED")
                .stream()
                .map(evaluation ->
                        evaluation.getMentorId()
                )
                .collect(Collectors.toSet());
    }
}