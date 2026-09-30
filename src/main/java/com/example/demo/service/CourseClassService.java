package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ContextUtil;
import com.example.demo.common.PageResult;
import com.example.demo.dto.courseclass.CourseClassRequest;
import com.example.demo.dto.courseclass.CourseClassResponse;
import com.example.demo.dto.courseclass.CourseClassUpdateRequest;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseClass;
import com.example.demo.entity.CourseClassStudent;
import com.example.demo.entity.GradeSheet;
import com.example.demo.entity.Semester;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.GradeSheetStatus;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseClassMapper;
import com.example.demo.mapper.CourseClassStudentMapper;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.mapper.GradeSheetMapper;
import com.example.demo.mapper.SemesterMapper;
import com.example.demo.mapper.TeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CourseClassService {

    private final CourseClassMapper courseClassMapper;
    private final CourseMapper courseMapper;
    private final SemesterMapper semesterMapper;
    private final TeacherMapper teacherMapper;
    private final GradeSheetMapper gradeSheetMapper;
    private final CourseClassStudentMapper relationMapper;

    public CourseClassService(
            CourseClassMapper courseClassMapper,
            CourseMapper courseMapper,
            SemesterMapper semesterMapper,
            TeacherMapper teacherMapper,
            GradeSheetMapper gradeSheetMapper,
            CourseClassStudentMapper relationMapper
    ) {
        this.courseClassMapper = courseClassMapper;
        this.courseMapper = courseMapper;
        this.semesterMapper = semesterMapper;
        this.teacherMapper = teacherMapper;
        this.gradeSheetMapper = gradeSheetMapper;
        this.relationMapper = relationMapper;
    }

    public PageResult<CourseClassResponse> page(
            long current,
            long size
    ) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<CourseClass> page = courseClassMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                Wrappers.<CourseClass>lambdaQuery()
                        .orderByDesc(CourseClass::getCreatedAt)
                        .orderByDesc(CourseClass::getId)
        );

        List<CourseClassResponse> records = page.getRecords()
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

    public List<CourseClassResponse> list() {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        return courseClassMapper.selectList(
                        Wrappers.<CourseClass>lambdaQuery()
                                .orderByAsc(CourseClass::getId)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseClassResponse detail(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        return toResponse(getCourseClass(id));
    }

    @Transactional
    public void create(CourseClassRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long courseId = parseId(request.getCourseId());
        Long semesterId = parseId(request.getSemesterId());
        Long teacherId = parseId(request.getTeacherId());
        String className = normalizeClassName(request.getClassName());

        validateReferences(courseId, semesterId, teacherId);
        validateWeights(request.getUsualWeight(), request.getExamWeight());

        if (businessKeyExists(
                courseId,
                semesterId,
                teacherId,
                className,
                null
        )) {
            throw new BusinessException(409, "该教学班已经存在");
        }

        CourseClass courseClass = new CourseClass();
        courseClass.setCourseId(courseId);
        courseClass.setSemesterId(semesterId);
        courseClass.setTeacherId(teacherId);
        courseClass.setClassName(className);
        courseClass.setUsualWeight(request.getUsualWeight());
        courseClass.setExamWeight(request.getExamWeight());
        courseClass.setStatus(request.getStatus());

        courseClassMapper.insert(courseClass);
    }

    @Transactional
    public void update(CourseClassUpdateRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long id = parseId(request.getId());
        Long courseId = parseId(request.getCourseId());
        Long semesterId = parseId(request.getSemesterId());
        Long teacherId = parseId(request.getTeacherId());
        String className = normalizeClassName(request.getClassName());

        CourseClass courseClass = getCourseClass(id);

        ensureCourseClassEditable(courseClass.getId());
        validateReferences(courseId, semesterId, teacherId);
        validateWeights(request.getUsualWeight(), request.getExamWeight());

        if (businessKeyExists(
                courseId,
                semesterId,
                teacherId,
                className,
                id
        )) {
            throw new BusinessException(409, "该教学班已经存在");
        }

        courseClass.setCourseId(courseId);
        courseClass.setSemesterId(semesterId);
        courseClass.setTeacherId(teacherId);
        courseClass.setClassName(className);
        courseClass.setUsualWeight(request.getUsualWeight());
        courseClass.setExamWeight(request.getExamWeight());
        courseClass.setStatus(request.getStatus());

        courseClassMapper.updateById(courseClass);
    }

    @Transactional
    public void delete(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        CourseClass courseClass = getCourseClass(id);
        ensureCourseClassDeletable(courseClass.getId());

        courseClassMapper.deleteById(courseClass.getId());
    }

    private void ensureCourseClassEditable(Long courseClassId) {
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
                    "成绩单已提交或锁定，不能修改教学班"
            );
        }
    }

    private void ensureCourseClassDeletable(Long courseClassId) {
        boolean gradeSheetExists = gradeSheetMapper.selectCount(
                Wrappers.<GradeSheet>lambdaQuery()
                        .eq(
                                GradeSheet::getCourseClassId,
                                courseClassId
                        )
        ) > 0;

        if (gradeSheetExists) {
            throw new BusinessException(
                    409,
                    "该教学班已经存在成绩单，不能删除"
            );
        }

        boolean hasStudents = relationMapper.selectCount(
                Wrappers.<CourseClassStudent>lambdaQuery()
                        .eq(
                                CourseClassStudent::getCourseClassId,
                                courseClassId
                        )
        ) > 0;

        if (hasStudents) {
            throw new BusinessException(
                    409,
                    "请先清空该教学班的学生名单"
            );
        }
    }

    private void validateReferences(
            Long courseId,
            Long semesterId,
            Long teacherId
    ) {
        Course course = courseMapper.selectById(courseId);

        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        Semester semester = semesterMapper.selectById(semesterId);

        if (semester == null) {
            throw new BusinessException(404, "学期不存在");
        }

        Teacher teacher = teacherMapper.selectById(teacherId);

        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }
    }

    private void validateWeights(
            BigDecimal usualWeight,
            BigDecimal examWeight
    ) {
        if (usualWeight.add(examWeight)
                .compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException(
                    "平时成绩权重和考试成绩权重之和必须等于 1"
            );
        }
    }

    private boolean businessKeyExists(
            Long courseId,
            Long semesterId,
            Long teacherId,
            String className,
            Long excludeId
    ) {
        LambdaQueryWrapper<CourseClass> wrapper =
                Wrappers.<CourseClass>lambdaQuery()
                        .eq(CourseClass::getCourseId, courseId)
                        .eq(CourseClass::getSemesterId, semesterId)
                        .eq(CourseClass::getTeacherId, teacherId)
                        .eq(CourseClass::getClassName, className);

        if (excludeId != null) {
            wrapper.ne(CourseClass::getId, excludeId);
        }

        return courseClassMapper.selectCount(wrapper) > 0;
    }

    private CourseClass getCourseClass(String id) {
        return getCourseClass(parseId(id));
    }

    private CourseClass getCourseClass(Long id) {
        CourseClass courseClass = courseClassMapper.selectById(id);

        if (courseClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        return courseClass;
    }

    private String normalizeClassName(String className) {
        return className.trim();
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException("ID 格式错误");
        }
    }

    private CourseClassResponse toResponse(CourseClass courseClass) {
        return new CourseClassResponse(
                String.valueOf(courseClass.getId()),
                String.valueOf(courseClass.getCourseId()),
                String.valueOf(courseClass.getSemesterId()),
                String.valueOf(courseClass.getTeacherId()),
                courseClass.getClassName(),
                courseClass.getUsualWeight(),
                courseClass.getExamWeight(),
                courseClass.getStatus(),
                courseClass.getCreatedAt()
        );
    }
}
