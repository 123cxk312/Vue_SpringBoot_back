package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.Result;
import com.example.demo.dto.course.CourseRequest;
import com.example.demo.dto.course.CourseResponse;
import com.example.demo.dto.course.CourseUpdateRequest;
import com.example.demo.service.CourseService;
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
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/page")
    public Result<PageResult<CourseResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(courseService.page(current, size));
    }

    @GetMapping("/list")
    public Result<List<CourseResponse>> list() {
        return Result.ok(courseService.list());
    }

    @GetMapping("/{id}")
    public Result<CourseResponse> detail(
            @PathVariable String id
    ) {
        return Result.ok(courseService.detail(id));
    }

    @PostMapping
    public Result<Void> create(
            @Valid @RequestBody CourseRequest request
    ) {
        courseService.create(request);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody CourseUpdateRequest request
    ) {
        courseService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id
    ) {
        courseService.delete(id);
        return Result.ok();
    }
}