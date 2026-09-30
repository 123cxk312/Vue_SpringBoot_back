package com.example.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String username;
    private String realName;
    private String roleCode;
    private String status;
    private LocalDateTime createdAt;
}