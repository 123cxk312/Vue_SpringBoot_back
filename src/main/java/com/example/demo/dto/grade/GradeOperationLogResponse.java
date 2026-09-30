package com.example.demo.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GradeOperationLogResponse {

    private String id;
    private String gradeSheetId;
    private String operatorId;
    private String action;
    private String beforeStatus;
    private String afterStatus;
    private String remark;
    private LocalDateTime createdAt;
}