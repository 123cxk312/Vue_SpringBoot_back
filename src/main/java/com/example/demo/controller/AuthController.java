package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.dto.auth.UserProfile;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserProfile> currentUser() {
        return Result.ok(authService.currentUser());
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 是客户端保存的无状态凭证，退出时由前端删除 Token。
        return Result.ok();
    }
}