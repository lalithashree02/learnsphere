package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;
import com.skill_learning_platform.skill_learning_platform.model.Course;
import com.skill_learning_platform.skill_learning_platform.model.ProjectSubmission;
import com.skill_learning_platform.skill_learning_platform.model.StudentTopicProgress;
import com.skill_learning_platform.skill_learning_platform.repository.CourseRepository;
import com.skill_learning_platform.skill_learning_platform.repository.CourseTopicRepository;
import com.skill_learning_platform.skill_learning_platform.repository.ProjectSubmissionRepository;
import com.skill_learning_platform.skill_learning_platform.repository.StudentTopicProgressRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectSubmissionService {

        private final ProjectSubmissionRepository projectSubmissionRepository;
        private final CourseRepository courseRepository;
        private final CourseTopicRepository courseTopicRepository;
        private final StudentTopicProgressRepository progressRepository;
        private final EnrollmentService enrollmentService;

        public ProjectSubmissionService(
                        ProjectSubmissionRepository projectSubmissionRepository,
                        CourseRepository courseRepository,
                        CourseTopicRepository courseTopicRepository,
                        StudentTopicProgressRepository progressRepository,
                        EnrollmentService enrollmentService) {

                this.projectSubmissionRepository = projectSubmissionRepository;
                this.courseRepository = courseRepository;
                this.courseTopicRepository = courseTopicRepository;
                this.progressRepository = progressRepository;
                this.enrollmentService = enrollmentService;
        }

        public ProjectSubmission submitProject(
        ProjectSubmission submission) {

    Long studentId =
            submission.getStudentId();

    Long courseId =
            submission.getCourseId();

    Course course = courseRepository.findById(courseId).orElse(null);
    if (course == null || course.getMentorId() == null) {
            throw new IllegalStateException(
                    "The selected course or its mentor could not be found.");
    }

    if (!enrollmentService.hasAccess(studentId, courseId)) {
            throw new IllegalStateException(
                    "An accepted course enrollment is required to submit a final project.");
    }


    // ==========================================
    // CHECK COURSE TOPICS
    // ==========================================

    List<CourseTopic> topics =
            courseTopicRepository
                    .findByCourseIdOrderByTopicNumberAsc(
                            courseId
                    );


    if (topics.isEmpty()) {

        throw new IllegalStateException(
                "This course does not have any topics yet."
        );
    }


    // ==========================================
    // GET STUDENT PROGRESS
    // ==========================================

    List<StudentTopicProgress> progressList =
            progressRepository
                    .findByStudentIdAndCourseId(
                            studentId,
                            courseId
                    );


    // ==========================================
    // COUNT COMPLETED TOPICS
    // ==========================================

    int completedTopics = 0;


    for (CourseTopic topic : topics) {

        for (StudentTopicProgress progress :
                progressList) {

            if (
                progress.getTopicId()
                        .equals(topic.getId())
                &&
                progress.isCompleted()
            ) {

                completedTopics++;

                break;
            }
        }
    }


    // ==========================================
    // CHECK ALL TOPICS COMPLETED
    // ==========================================

    if (completedTopics < topics.size()) {

        throw new IllegalStateException(
                "Complete all course topics before submitting the final project."
        );
    }


    // ==========================================
    // CHECK EXISTING SUBMISSION
    // ==========================================

    ProjectSubmission existingSubmission =
            projectSubmissionRepository
                    .findByStudentIdAndCourseId(
                            studentId,
                            courseId
                    )
                    .orElse(null);


    // ==========================================
    // IF PREVIOUS SUBMISSION EXISTS
    // ==========================================

    if (existingSubmission != null) {

        String existingStatus =
                existingSubmission.getStatus();


        // --------------------------------------
        // Allow resubmission only when mentor
        // requested changes
        // --------------------------------------

        if (
            !"CHANGES_REQUIRED"
                    .equalsIgnoreCase(
                            existingStatus
                    )
        ) {

            throw new IllegalStateException(
                    "You cannot submit another project while the current submission is under review or already approved."
            );
        }


        // --------------------------------------
        // Update existing submission
        // --------------------------------------

        existingSubmission.setGithubUrl(
                submission.getGithubUrl()
        );

        existingSubmission.setMentorId(course.getMentorId());

        existingSubmission.setStatus(
                "PENDING_REVIEW"
        );

        existingSubmission.setMentorFeedback(
                null
        );

        existingSubmission.setSubmittedDate(
                LocalDate.now().toString()
        );

        existingSubmission.setReviewedDate(
                null
        );


        return projectSubmissionRepository.save(
                existingSubmission
        );
    }


    // ==========================================
    // FIRST-TIME SUBMISSION
    // ==========================================

    submission.setStatus(
            "PENDING_REVIEW"
    );

    submission.setMentorId(course.getMentorId());

    submission.setMentorFeedback(
            null
    );

    submission.setSubmittedDate(
            LocalDate.now().toString()
    );

    submission.setReviewedDate(
            null
    );


    return projectSubmissionRepository.save(
            submission
    );
}

        public List<ProjectSubmission> getStudentSubmissions(Long studentId) {
                return projectSubmissionRepository.findByStudentId(studentId);
        }

        public List<ProjectSubmission> getMentorSubmissions(Long mentorId) {
                return projectSubmissionRepository.findByMentorId(mentorId);
        }

        public ProjectSubmission getSubmissionById(Long id) {
                return projectSubmissionRepository.findById(id).orElse(null);
        }
}