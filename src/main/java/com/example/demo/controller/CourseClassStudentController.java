package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.Result;
import com.example.demo.dto.courseclassstudent.CourseClassStudentRequest;
import com.example.demo.dto.courseclassstudent.CourseClassStudentResponse;
import com.example.demo.dto.courseclassstudent.CourseClassStudentUpdateRequest;
import com.example.demo.service.CourseClassStudentService;
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
@RequestMapping("/course-class-students")
public class CourseClassStudentController {

    private final CourseClassStudentService relationService;

    public CourseClassStudentController(
            CourseClassStudentService relationService
    ) {
        this.relationService = relationService;
    }

    @GetMapping("/page")
    public Result<PageResult<CourseClassStudentResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(relationService.page(current, size));
    }

    @GetMapping("/list")
    public Result<List<CourseClassStudentResponse>> list() {
        return Result.ok(relationService.list());
    }

    @GetMapping("/{id}")
    public Result<CourseClassStudentResponse> detail(
            @PathVariable String id
    ) {
        return Result.ok(relationService.detail(id));
    }

    @PostMapping
    public Result<Void> create(
            @Valid @RequestBody CourseClassStudentRequest request
    ) {
        relationService.create(request);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody
            CourseClassStudentUpdateRequest request
    ) {
        relationService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id
    ) {
        relationService.delete(id);
        return Result.ok();
    }
}