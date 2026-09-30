package com.example.demo.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.ContextUtil;
import com.example.demo.dto.teacher.TeacherCourseClassResponse;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseClass;
import com.example.demo.entity.Semester;
import com.example.demo.entity.SysUser;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.AuthException;
import com.example.demo.mapper.CourseClassMapper;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.mapper.SemesterMapper;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TeacherCourseClassService {

    private final CourseClassMapper courseClassMapper;
    private final CourseMapper courseMapper;
    private final SemesterMapper semesterMapper;
    private final TeacherMapper teacherMapper;
    private final SysUserMapper sysUserMapper;

    public TeacherCourseClassService(
            CourseClassMapper courseClassMapper,
            CourseMapper courseMapper,
            SemesterMapper semesterMapper,
            TeacherMapper teacherMapper,
            SysUserMapper sysUserMapper
    ) {
        this.courseClassMapper = courseClassMapper;
        this.courseMapper = courseMapper;
        this.semesterMapper = semesterMapper;
        this.teacherMapper = teacherMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public List<TeacherCourseClassResponse> listForCurrentTeacher() {
        ContextUtil.requireRole(RoleEnum.TEACHER);

        Long userId = ContextUtil.getUserId();

        Teacher teacher = teacherMapper.selectOne(
                Wrappers.<Teacher>lambdaQuery()
                        .eq(Teacher::getUserId, userId)
        );

        if (teacher == null) {
            throw new AuthException(403, "当前账号没有教师资料");
        }

        List<CourseClass> courseClasses = courseClassMapper.selectList(
                Wrappers.<CourseClass>lambdaQuery()
                        .eq(CourseClass::getTeacherId, teacher.getId())
                        .orderByDesc(CourseClass::getCreatedAt)
                        .orderByDesc(CourseClass::getId)
        );

        if (courseClasses.isEmpty()) {
            return List.of();
        }

        List<Long> courseIds = courseClasses.stream()
                .map(CourseClass::getCourseId)
                .distinct()
                .toList();

        List<Long> semesterIds = courseClasses.stream()
                .map(CourseClass::getSemesterId)
                .distinct()
                .toList();

        Map<Long, Course> courseMap = courseMapper
                .selectBatchIds(courseIds)
                .stream()
                .collect(Collectors.toMap(
                        Course::getId,
                        Function.identity()
                ));

        Map<Long, Semester> semesterMap = semesterMapper
                .selectBatchIds(semesterIds)
                .stream()
                .collect(Collectors.toMap(
                        Semester::getId,
                        Function.identity()
                ));

        SysUser user = sysUserMapper.selectById(userId);
        String teacherName = user == null ? "未知教师" : user.getRealName();

        return courseClasses.stream()
                .map(courseClass -> {
                    Course course = courseMap.get(
                            courseClass.getCourseId()
                    );
                    Semester semester = semesterMap.get(
                            courseClass.getSemesterId()
                    );

                    return new TeacherCourseClassResponse(
                            String.valueOf(courseClass.getId()),
                            String.valueOf(courseClass.getCourseId()),
                            course == null
                                    ? ""
                                    : course.getCourseCode(),
                            course == null
                                    ? ""
                                    : course.getCourseName(),
                            String.valueOf(courseClass.getSemesterId()),
                            semester == null
                                    ? ""
                                    : semester.getSemesterName(),
                            String.valueOf(courseClass.getTeacherId()),
                            teacherName,
                            courseClass.getClassName(),
                            courseClass.getUsualWeight(),
                            courseClass.getExamWeight(),
                            courseClass.getStatus(),
                            courseClass.getCreatedAt()
                    );
                })
                .toList();
    }
}