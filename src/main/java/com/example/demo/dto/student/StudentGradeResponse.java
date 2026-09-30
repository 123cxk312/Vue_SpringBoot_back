package com.example.demo.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StudentGradeResponse {

    private String courseCode;
    private String courseName;
    private String semesterName;
    private String courseClassName;
    private BigDecimal credit;
    private BigDecimal usualScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private LocalDateTime publishedAt;
}