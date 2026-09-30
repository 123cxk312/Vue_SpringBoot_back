package com.example.demo.dto.course;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CourseResponse {

    private String id;
    private String courseCode;
    private String courseName;
    private BigDecimal credit;
    private String status;
    private LocalDateTime createdAt;
}