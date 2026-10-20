package com.skill_learning_platform.skill_learning_platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private Long courseId;

    private Long mentorId;

    private String certificateNumber;

    private String studentName;

    private String courseName;

    private String skill;

    private String mentorName;

    private String issuedDate;

    private String mentorSignature;

    private String adminSignature;

    private String status;


    public Certificate() {
    }


    public Certificate(
            Long studentId,
            Long courseId,
            Long mentorId,
            String certificateNumber,
            String studentName,
            String courseName,
            String skill,
            String mentorName,
            String issuedDate,
            String mentorSignature,
            String adminSignature,
            String status) {

        this.studentId = studentId;
        this.courseId = courseId;
        this.mentorId = mentorId;
        this.certificateNumber = certificateNumber;
        this.studentName = studentName;
        this.courseName = courseName;
        this.skill = skill;
        this.mentorName = mentorName;
        this.issuedDate = issuedDate;
        this.mentorSignature = mentorSignature;
        this.adminSignature = adminSignature;
        this.status = status;
    }


    public Long getId() {
        return id;
    }


    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }


    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }


    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }


    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(
            String certificateNumber) {

        this.certificateNumber =
                certificateNumber;
    }


    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }


    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }


    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }


    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }


    public String getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }


    public String getMentorSignature() {
        return mentorSignature;
    }

    public void setMentorSignature(
            String mentorSignature) {

        this.mentorSignature =
                mentorSignature;
    }


    public String getAdminSignature() {
        return adminSignature;
    }

    public void setAdminSignature(
            String adminSignature) {

        this.adminSignature =
                adminSignature;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
