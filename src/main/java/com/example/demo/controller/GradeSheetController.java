package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.grade.GradeOperationLogResponse;
import com.example.demo.dto.grade.GradeSheetDetailResponse;
import com.example.demo.dto.grade.ReviewRequest;
import com.example.demo.dto.grade.ScoreSaveRequest;
import com.example.demo.service.GradeSheetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/grade-sheets")
public class GradeSheetController {

    private final GradeSheetService gradeSheetService;

    public GradeSheetController(
            GradeSheetService gradeSheetService
    ) {
        this.gradeSheetService = gradeSheetService;
    }

    @GetMapping("/course-class/{courseClassId}")
    public Result<GradeSheetDetailResponse> detailByCourseClass(
            @PathVariable String courseClassId
    ) {
        return Result.ok(
                gradeSheetService.detailByCourseClass(
                        courseClassId
                )
        );
    }

    @PutMapping("/{id}/scores")
    public Result<GradeSheetDetailResponse> saveScores(
            @PathVariable String id,
            @Valid @RequestBody ScoreSaveRequest request
    ) {
        return Result.ok(
                gradeSheetService.saveScores(id, request)
        );
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable String id) {
        gradeSheetService.submit(id);
        return Result.ok();
    }

    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable String id) {
        gradeSheetService.review(id);
        return Result.ok();
    }

    @PostMapping("/{id}/return")
    public Result<Void> returnSheet(
            @PathVariable String id,
            @Valid @RequestBody ReviewRequest request
    ) {
        gradeSheetService.returnSheet(id, request);
        return Result.ok();
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable String id) {
        gradeSheetService.publish(id);
        return Result.ok();
    }

    @GetMapping("/{id}/logs")
    public Result<List<GradeOperationLogResponse>> logs(
            @PathVariable String id
    ) {
        return Result.ok(gradeSheetService.logs(id));
    }
}