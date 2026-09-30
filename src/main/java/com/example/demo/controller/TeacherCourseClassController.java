package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.teacher.TeacherCourseClassResponse;
import com.example.demo.service.TeacherCourseClassService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/teacher/course-classes")
public class TeacherCourseClassController {

    private final TeacherCourseClassService teacherCourseClassService;

    public TeacherCourseClassController(
            TeacherCourseClassService teacherCourseClassService
    ) {
        this.teacherCourseClassService = teacherCourseClassService;
    }

    @GetMapping
    public Result<List<TeacherCourseClassResponse>> list() {
        return Result.ok(
                teacherCourseClassService.listForCurrentTeacher()
        );
    }
}