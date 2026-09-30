package com.example.demo.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TeacherResponse {

    private String id;
    private String userId;
    private String teacherNo;
    private String title;
    private LocalDateTime createdAt;
}