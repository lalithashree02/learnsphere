
package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.CourseTopic;
import com.skill_learning_platform.skill_learning_platform.repository.CourseTopicRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseTopicService {

    private final CourseTopicRepository courseTopicRepository;

    public CourseTopicService(
            CourseTopicRepository courseTopicRepository) {

        this.courseTopicRepository =
                courseTopicRepository;
    }

    // ============================================================
    // CREATE TOPIC
    // ============================================================

    public CourseTopic createTopic(CourseTopic topic) {

        List<CourseTopic> existing =
                getTopicsByCourse(topic.getCourseId());

        int nextTopicNumber =
                existing.stream()
                        .mapToInt(existingTopic ->
                                existingTopic.getTopicNumber())
                        .max()
                        .orElse(0) + 1;

        topic.setTopicNumber(nextTopicNumber);

        if (topic.getContentType() == null
                || topic.getContentType().isBlank()) {

            topic.setContentType("TEXT");

        } else {

            topic.setContentType(
                    topic.getContentType()
                            .trim()
                            .toUpperCase()
            );
        }

        return courseTopicRepository.save(topic);
    }

    // ============================================================
    // GET ALL TOPICS
    // ============================================================

    public List<CourseTopic> getAllTopics() {

        return courseTopicRepository.findAll();
    }

    // ============================================================
    // GET ONE TOPIC
    // ============================================================

    public CourseTopic getTopicById(Long id) {

        return courseTopicRepository
                .findById(id)
                .orElse(null);
    }

    // ============================================================
    // GET ALL TOPICS OF A COURSE
    // ============================================================

    public List<CourseTopic> getTopicsByCourse(
            Long courseId) {

        return courseTopicRepository
                .findByCourseIdOrderByTopicNumberAsc(courseId);
    }

    // ============================================================
    // UPDATE TOPIC
    // ============================================================

    public CourseTopic updateTopic(
            Long id,
            CourseTopic updatedTopic) {

        CourseTopic topic =
                courseTopicRepository
                        .findById(id)
                        .orElse(null);

        if (topic == null) {
            return null;
        }

        topic.setCourseId(
                updatedTopic.getCourseId()
        );

        topic.setTopicNumber(
                updatedTopic.getTopicNumber()
        );

        topic.setTopicTitle(
                updatedTopic.getTopicTitle()
        );

        topic.setContentType(
                updatedTopic.getContentType()
        );

        topic.setContent(
                updatedTopic.getContent()
        );

        topic.setVideoUrl(
                updatedTopic.getVideoUrl()
        );

        topic.setMeetingTitle(
                updatedTopic.getMeetingTitle()
        );

        topic.setMeetingUrl(
                updatedTopic.getMeetingUrl()
        );

        topic.setMeetingSchedule(
                updatedTopic.getMeetingSchedule()
        );

        return courseTopicRepository.save(topic);
    }

    // ============================================================
    // DELETE TOPIC
    // ============================================================

    public boolean deleteTopic(Long id) {

        if (!courseTopicRepository.existsById(id)) {
            return false;
        }

        courseTopicRepository.deleteById(id);

        return true;
    }
}