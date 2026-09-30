package com.example.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfile {

    private String id;
    private String username;
    private String realName;
    private String roleCode;
    private String status;
    private String studentId;
    private String teacherId;
}