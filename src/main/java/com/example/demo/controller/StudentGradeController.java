package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.student.StudentGradeResponse;
import com.example.demo.service.StudentGradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/grades")
public class StudentGradeController {

    private final StudentGradeService studentGradeService;

    public StudentGradeController(
            StudentGradeService studentGradeService
    ) {
        this.studentGradeService = studentGradeService;
    }

    @GetMapping
    public Result<List<StudentGradeResponse>> list() {
        return Result.ok(
                studentGradeService.listForCurrentStudent()
        );
    }
}