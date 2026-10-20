package com.skill_learning_platform.skill_learning_platform.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_content_test_results")
public class CourseContentTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long courseId;
    private Long contentId;
    private int totalMarks;
    private int marksObtained;
    private double percentage;

    public CourseContentTestResult() { }

    public CourseContentTestResult(Long studentId, Long courseId, Long contentId,
                                   int totalMarks, int marksObtained, double percentage) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.contentId = contentId;
        this.totalMarks = totalMarks;
        this.marksObtained = marksObtained;
        this.percentage = percentage;
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public Long getCourseId() { return courseId; }
    public Long getContentId() { return contentId; }
    public int getTotalMarks() { return totalMarks; }
    public int getMarksObtained() { return marksObtained; }
    public double getPercentage() { return percentage; }
}