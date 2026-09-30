package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.Result;
import com.example.demo.dto.user.UserCreateRequest;
import com.example.demo.dto.user.UserResponse;
import com.example.demo.dto.user.UserUpdateRequest;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public Result<PageResult<UserResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(userService.page(current, size));
    }

    @GetMapping("/list")
    public Result<List<UserResponse>> list() {
        return Result.ok(userService.list());
    }

    @GetMapping("/{id}")
    public Result<UserResponse> detail(
            @PathVariable String id
    ) {
        return Result.ok(userService.detail(id));
    }

    @PostMapping
    public Result<Void> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        userService.create(request);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody UserUpdateRequest request
    ) {
        userService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id
    ) {
        userService.delete(id);
        return Result.ok();
    }
}