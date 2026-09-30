package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.Result;
import com.example.demo.dto.teacher.TeacherRequest;
import com.example.demo.dto.teacher.TeacherResponse;
import com.example.demo.dto.teacher.TeacherUpdateRequest;
import com.example.demo.service.TeacherService;
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
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/page")
    public Result<PageResult<TeacherResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(teacherService.page(current, size));
    }

    @GetMapping("/list")
    public Result<List<TeacherResponse>> list() {
        return Result.ok(teacherService.list());
    }

    @GetMapping("/{id}")
    public Result<TeacherResponse> detail(
            @PathVariable String id
    ) {
        return Result.ok(teacherService.detail(id));
    }

    @PostMapping
    public Result<Void> create(
            @Valid @RequestBody TeacherRequest request
    ) {
        teacherService.create(request);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody TeacherUpdateRequest request
    ) {
        teacherService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id
    ) {
        teacherService.delete(id);
        return Result.ok();
    }
}