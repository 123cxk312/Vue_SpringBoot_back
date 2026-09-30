package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.Result;
import com.example.demo.dto.semester.SemesterRequest;
import com.example.demo.dto.semester.SemesterResponse;
import com.example.demo.dto.semester.SemesterUpdateRequest;
import com.example.demo.service.SemesterService;
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
@RequestMapping("/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping("/page")
    public Result<PageResult<SemesterResponse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        return Result.ok(semesterService.page(current, size));
    }

    @GetMapping("/list")
    public Result<List<SemesterResponse>> list() {
        return Result.ok(semesterService.list());
    }

    @GetMapping("/{id}")
    public Result<SemesterResponse> detail(
            @PathVariable String id
    ) {
        return Result.ok(semesterService.detail(id));
    }

    @PostMapping
    public Result<Void> create(
            @Valid @RequestBody SemesterRequest request
    ) {
        semesterService.create(request);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody SemesterUpdateRequest request
    ) {
        semesterService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable String id
    ) {
        semesterService.delete(id);
        return Result.ok();
    }
}