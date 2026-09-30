package com.example.demo.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StudentResponse {

    private String id;
    private String userId;
    private String studentNo;
    private String className;
    private String major;
    private Integer gradeYear;
    private LocalDateTime createdAt;
}