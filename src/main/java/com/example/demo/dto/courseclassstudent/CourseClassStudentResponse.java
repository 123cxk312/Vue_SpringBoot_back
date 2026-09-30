package com.example.demo.dto.courseclassstudent;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CourseClassStudentResponse {

    private String id;
    private String courseClassId;
    private String studentId;
    private LocalDateTime createdAt;
}