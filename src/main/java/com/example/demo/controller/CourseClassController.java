package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.Result;
import com.example.demo.dto.courseclass.CourseClassRequest;
import com.example.demo.dto.courseclass.CourseClassResponse;
import com.example.demo.dto.courseclass.CourseClassUpdateRequest;
import com.example.demo.service.CourseClassService;
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
@RequestMapping("/course-classes")
public class CourseClassController {

    private final CourseClassService courseClassService;

    public CourseClassController(
            CourseClassService courseClassService
    ) {
        this.courseClassService = courseClassService;
    }

    @GetMapping("/page")
    public Result<PageResult<CourseClassResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(courseClassService.page(current, size));
    }

    @GetMapping("/list")
    public Result<List<CourseClassResponse>> list() {
        return Result.ok(courseClassService.list());
    }

    @GetMapping("/{id}")
    public Result<CourseClassResponse> detail(
            @PathVariable String id
    ) {
        return Result.ok(courseClassService.detail(id));
    }

    @PostMapping
    public Result<Void> create(
            @Valid @RequestBody CourseClassRequest request
    ) {
        courseClassService.create(request);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody CourseClassUpdateRequest request
    ) {
        courseClassService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id
    ) {
        courseClassService.delete(id);
        return Result.ok();
    }
}