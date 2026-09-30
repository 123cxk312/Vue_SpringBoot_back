package com.example.demo.dto.courseclass;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CourseClassResponse {

    private String id;
    private String courseId;
    private String semesterId;
    private String teacherId;
    private String className;
    private BigDecimal usualWeight;
    private BigDecimal examWeight;
    private String status;
    private LocalDateTime createdAt;
}