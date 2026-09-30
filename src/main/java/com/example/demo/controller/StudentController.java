package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.Result;
import com.example.demo.dto.student.StudentRequest;
import com.example.demo.dto.student.StudentResponse;
import com.example.demo.dto.student.StudentUpdateRequest;
import com.example.demo.service.StudentService;
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
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/page")
    public Result<PageResult<StudentResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(studentService.page(current, size));
    }

    @GetMapping("/list")
    public Result<List<StudentResponse>> list() {
        return Result.ok(studentService.list());
    }

    @GetMapping("/{id}")
    public Result<StudentResponse> detail(
            @PathVariable String id
    ) {
        return Result.ok(studentService.detail(id));
    }

    @PostMapping
    public Result<Void> create(
            @Valid @RequestBody StudentRequest request
    ) {
        studentService.create(request);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody StudentUpdateRequest request
    ) {
        studentService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id
    ) {
        studentService.delete(id);
        return Result.ok();
    }
}