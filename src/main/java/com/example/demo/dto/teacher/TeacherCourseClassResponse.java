package com.example.demo.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TeacherCourseClassResponse {

    private String id;
    private String courseId;
    private String courseCode;
    private String courseName;
    private String semesterId;
    private String semesterName;
    private String teacherId;
    private String teacherName;
    private String className;
    private BigDecimal usualWeight;
    private BigDecimal examWeight;
    private String status;
    private LocalDateTime createdAt;
}