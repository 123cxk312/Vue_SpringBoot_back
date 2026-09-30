package com.example.demo.common;

import com.example.demo.enums.RoleEnum;

public class UserContext {

    private final Long userId;
    private final RoleEnum role;

    public UserContext(Long userId, RoleEnum role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public RoleEnum getRole() {
        return role;
    }
}