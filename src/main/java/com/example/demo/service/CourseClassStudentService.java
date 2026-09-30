package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ContextUtil;
import com.example.demo.common.PageResult;
import com.example.demo.dto.courseclassstudent.CourseClassStudentRequest;
import com.example.demo.dto.courseclassstudent.CourseClassStudentResponse;
import com.example.demo.dto.courseclassstudent.CourseClassStudentUpdateRequest;
import com.example.demo.entity.CourseClass;
import com.example.demo.entity.CourseClassStudent;
import com.example.demo.entity.GradeSheet;
import com.example.demo.entity.Student;
import com.example.demo.enums.GradeSheetStatus;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseClassMapper;
import com.example.demo.mapper.CourseClassStudentMapper;
import com.example.demo.mapper.GradeSheetMapper;
import com.example.demo.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseClassStudentService {

    private final CourseClassStudentMapper relationMapper;
    private final CourseClassMapper courseClassMapper;
    private final StudentMapper studentMapper;
    private final GradeSheetMapper gradeSheetMapper;

    public CourseClassStudentService(
            CourseClassStudentMapper relationMapper,
            CourseClassMapper courseClassMapper,
            StudentMapper studentMapper,
            GradeSheetMapper gradeSheetMapper
    ) {
        this.relationMapper = relationMapper;
        this.courseClassMapper = courseClassMapper;
        this.studentMapper = studentMapper;
        this.gradeSheetMapper = gradeSheetMapper;
    }

    public PageResult<CourseClassStudentResponse> page(
            long current,
            long size
    ) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<CourseClassStudent> page = relationMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                Wrappers.<CourseClassStudent>lambdaQuery()
                        .orderByDesc(CourseClassStudent::getCreatedAt)
                        .orderByDesc(CourseClassStudent::getId)
        );

        List<CourseClassStudentResponse> records = page.getRecords()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                records
        );
    }

    public List<CourseClassStudentResponse> list() {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        return relationMapper.selectList(
                        Wrappers.<CourseClassStudent>lambdaQuery()
                                .orderByDesc(CourseClassStudent::getCreatedAt)
                                .orderByDesc(CourseClassStudent::getId)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseClassStudentResponse detail(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        return toResponse(getRelation(id));
    }

    @Transactional
    public void create(CourseClassStudentRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long courseClassId = parseId(request.getCourseClassId());
        Long studentId = parseId(request.getStudentId());

        validateReferences(courseClassId, studentId);
        ensureEnrollmentEditable(courseClassId);

        if (relationExists(courseClassId, studentId, null)) {
            throw new BusinessException(409, "该学生已经在此教学班中");
        }

        CourseClassStudent relation = new CourseClassStudent();
        relation.setCourseClassId(courseClassId);
        relation.setStudentId(studentId);

        relationMapper.insert(relation);
    }

    @Transactional
    public void update(CourseClassStudentUpdateRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long id = parseId(request.getId());
        Long courseClassId = parseId(request.getCourseClassId());
        Long studentId = parseId(request.getStudentId());

        CourseClassStudent relation = getRelation(id);

        validateReferences(courseClassId, studentId);
        ensureEnrollmentEditable(relation.getCourseClassId());
        ensureEnrollmentEditable(courseClassId);

        if (relationExists(courseClassId, studentId, id)) {
            throw new BusinessException(409, "该学生已经在此教学班中");
        }

        relation.setCourseClassId(courseClassId);
        relation.setStudentId(studentId);

        relationMapper.updateById(relation);
    }

    @Transactional
    public void delete(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        CourseClassStudent relation = getRelation(id);
        ensureEnrollmentEditable(relation.getCourseClassId());

        relationMapper.deleteById(relation.getId());
    }

    private void ensureEnrollmentEditable(Long courseClassId) {
        GradeSheet gradeSheet = gradeSheetMapper.selectOne(
                Wrappers.<GradeSheet>lambdaQuery()
                        .eq(
                                GradeSheet::getCourseClassId,
                                courseClassId
                        )
        );

        if (gradeSheet == null) {
            return;
        }

        GradeSheetStatus status =
                GradeSheetStatus.fromCode(gradeSheet.getStatus());

        if (status != GradeSheetStatus.DRAFT
                && status != GradeSheetStatus.RETURNED) {
            throw new BusinessException(
                    409,
                    "成绩单已提交或锁定，不能修改选课名单"
            );
        }
    }

    private void validateReferences(
            Long courseClassId,
            Long studentId
    ) {
        CourseClass courseClass =
                courseClassMapper.selectById(courseClassId);

        if (courseClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        Student student = studentMapper.selectById(studentId);

        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
    }

    private boolean relationExists(
            Long courseClassId,
            Long studentId,
            Long excludeId
    ) {
        LambdaQueryWrapper<CourseClassStudent> wrapper =
                Wrappers.<CourseClassStudent>lambdaQuery()
                        .eq(
                                CourseClassStudent::getCourseClassId,
                                courseClassId
                        )
                        .eq(
                                CourseClassStudent::getStudentId,
                                studentId
                        );

        if (excludeId != null) {
            wrapper.ne(CourseClassStudent::getId, excludeId);
        }

        return relationMapper.selectCount(wrapper) > 0;
    }

    private CourseClassStudent getRelation(String id) {
        return getRelation(parseId(id));
    }

    private CourseClassStudent getRelation(Long id) {
        CourseClassStudent relation = relationMapper.selectById(id);

        if (relation == null) {
            throw new BusinessException(404, "选课关系不存在");
        }

        return relation;
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException("ID 格式错误");
        }
    }

    private CourseClassStudentResponse toResponse(
            CourseClassStudent relation
    ) {
        return new CourseClassStudentResponse(
                String.valueOf(relation.getId()),
                String.valueOf(relation.getCourseClassId()),
                String.valueOf(relation.getStudentId()),
                relation.getCreatedAt()
        );
    }
}