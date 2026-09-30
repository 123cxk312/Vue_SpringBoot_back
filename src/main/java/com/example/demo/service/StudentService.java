package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ContextUtil;
import com.example.demo.common.PageResult;
import com.example.demo.dto.student.StudentRequest;
import com.example.demo.dto.student.StudentResponse;
import com.example.demo.dto.student.StudentUpdateRequest;
import com.example.demo.entity.CourseClassStudent;
import com.example.demo.entity.Score;
import com.example.demo.entity.Student;
import com.example.demo.entity.SysUser;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseClassStudentMapper;
import com.example.demo.mapper.ScoreMapper;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class StudentService {

    private final StudentMapper studentMapper;
    private final SysUserMapper sysUserMapper;
    private final CourseClassStudentMapper relationMapper;
    private final ScoreMapper scoreMapper;

    public StudentService(
            StudentMapper studentMapper,
            SysUserMapper sysUserMapper,
            CourseClassStudentMapper relationMapper,
            ScoreMapper scoreMapper
    ) {
        this.studentMapper = studentMapper;
        this.sysUserMapper = sysUserMapper;
        this.relationMapper = relationMapper;
        this.scoreMapper = scoreMapper;
    }

    public PageResult<StudentResponse> page(long current, long size) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<Student> page = studentMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                Wrappers.<Student>lambdaQuery()
                        .orderByDesc(Student::getCreatedAt)
                        .orderByDesc(Student::getId)
        );

        List<StudentResponse> records = page.getRecords()
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

    public List<StudentResponse> list() {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        return studentMapper.selectList(
                        Wrappers.<Student>lambdaQuery()
                                .orderByAsc(Student::getStudentNo)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentResponse detail(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);
        return toResponse(getStudent(id));
    }

    @Transactional
    public void create(StudentRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long userId = parseId(request.getUserId());
        String studentNo = normalizeStudentNo(request.getStudentNo());

        validateUser(userId, null);

        if (studentNoExists(studentNo, null)) {
            throw new BusinessException(409, "学号已经存在");
        }

        Student student = new Student();
        student.setUserId(userId);
        student.setStudentNo(studentNo);
        student.setClassName(request.getClassName().trim());
        student.setMajor(request.getMajor().trim());
        student.setGradeYear(request.getGradeYear());

        studentMapper.insert(student);
    }

    @Transactional
    public void update(StudentUpdateRequest request) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Long id = parseId(request.getId());
        Long userId = parseId(request.getUserId());
        Student student = getStudent(id);
        String studentNo = normalizeStudentNo(request.getStudentNo());

        validateUser(userId, id);

        if (studentNoExists(studentNo, id)) {
            throw new BusinessException(409, "学号已经存在");
        }

        student.setUserId(userId);
        student.setStudentNo(studentNo);
        student.setClassName(request.getClassName().trim());
        student.setMajor(request.getMajor().trim());
        student.setGradeYear(request.getGradeYear());

        studentMapper.updateById(student);
    }

    @Transactional
    public void delete(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        Student student = getStudent(id);

        if (relationMapper.selectCount(
                Wrappers.<CourseClassStudent>lambdaQuery()
                        .eq(CourseClassStudent::getStudentId, student.getId())
        ) > 0) {
            throw new BusinessException(
                    409,
                    "请先将该学生移出所有教学班"
            );
        }

        if (scoreMapper.selectCount(
                Wrappers.<Score>lambdaQuery()
                        .eq(Score::getStudentId, student.getId())
        ) > 0) {
            throw new BusinessException(
                    409,
                    "该学生已有成绩记录，不能删除"
            );
        }

        studentMapper.deleteById(student.getId());
    }

    private void validateUser(Long userId, Long excludeStudentId) {
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(404, "关联用户不存在");
        }

        if (!RoleEnum.STUDENT.getCode().equals(user.getRoleCode())) {
            throw new BusinessException("关联用户的角色必须是 STUDENT");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("关联用户账号不可用");
        }

        LambdaQueryWrapper<Student> wrapper =
                Wrappers.<Student>lambdaQuery()
                        .eq(Student::getUserId, userId);

        if (excludeStudentId != null) {
            wrapper.ne(Student::getId, excludeStudentId);
        }

        if (studentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "该用户已经关联学生资料");
        }
    }

    private boolean studentNoExists(
            String studentNo,
            Long excludeId
    ) {
        LambdaQueryWrapper<Student> wrapper =
                Wrappers.<Student>lambdaQuery()
                        .eq(Student::getStudentNo, studentNo);

        if (excludeId != null) {
            wrapper.ne(Student::getId, excludeId);
        }

        return studentMapper.selectCount(wrapper) > 0;
    }

    private Student getStudent(String id) {
        return getStudent(parseId(id));
    }

    private Student getStudent(Long id) {
        Student student = studentMapper.selectById(id);

        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        return student;
    }

    private String normalizeStudentNo(String studentNo) {
        return studentNo.trim().toUpperCase(Locale.ROOT);
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException("ID 格式错误");
        }
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                String.valueOf(student.getId()),
                String.valueOf(student.getUserId()),
                student.getStudentNo(),
                student.getClassName(),
                student.getMajor(),
                student.getGradeYear(),
                student.getCreatedAt()
        );
    }
}
