package com.example.demo.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.ContextUtil;
import com.example.demo.dto.student.StudentGradeResponse;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseClass;
import com.example.demo.entity.CourseClassStudent;
import com.example.demo.entity.GradeSheet;
import com.example.demo.entity.Score;
import com.example.demo.entity.Semester;
import com.example.demo.entity.Student;
import com.example.demo.enums.GradeSheetStatus;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.AuthException;
import com.example.demo.mapper.CourseClassMapper;
import com.example.demo.mapper.CourseClassStudentMapper;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.mapper.GradeSheetMapper;
import com.example.demo.mapper.ScoreMapper;
import com.example.demo.mapper.SemesterMapper;
import com.example.demo.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudentGradeService {

    private final StudentMapper studentMapper;
    private final CourseClassStudentMapper relationMapper;
    private final GradeSheetMapper gradeSheetMapper;
    private final ScoreMapper scoreMapper;
    private final CourseClassMapper courseClassMapper;
    private final CourseMapper courseMapper;
    private final SemesterMapper semesterMapper;

    public StudentGradeService(
            StudentMapper studentMapper,
            CourseClassStudentMapper relationMapper,
            GradeSheetMapper gradeSheetMapper,
            ScoreMapper scoreMapper,
            CourseClassMapper courseClassMapper,
            CourseMapper courseMapper,
            SemesterMapper semesterMapper
    ) {
        this.studentMapper = studentMapper;
        this.relationMapper = relationMapper;
        this.gradeSheetMapper = gradeSheetMapper;
        this.scoreMapper = scoreMapper;
        this.courseClassMapper = courseClassMapper;
        this.courseMapper = courseMapper;
        this.semesterMapper = semesterMapper;
    }

    public List<StudentGradeResponse> listForCurrentStudent() {
        ContextUtil.requireRole(RoleEnum.STUDENT);

        Long userId = ContextUtil.getUserId();

        Student student = studentMapper.selectOne(
                Wrappers.<Student>lambdaQuery()
                        .eq(Student::getUserId, userId)
        );

        if (student == null) {
            throw new AuthException(403, "当前账号没有学生资料");
        }

        List<Long> courseClassIds = relationMapper.selectList(
                        Wrappers.<CourseClassStudent>lambdaQuery()
                                .eq(
                                        CourseClassStudent::getStudentId,
                                        student.getId()
                                )
                )
                .stream()
                .map(CourseClassStudent::getCourseClassId)
                .distinct()
                .toList();

        if (courseClassIds.isEmpty()) {
            return List.of();
        }

        List<GradeSheet> gradeSheets = gradeSheetMapper.selectList(
                Wrappers.<GradeSheet>lambdaQuery()
                        .in(
                                GradeSheet::getCourseClassId,
                                courseClassIds
                        )
                        .eq(
                                GradeSheet::getStatus,
                                GradeSheetStatus.PUBLISHED.getCode()
                        )
        );

        if (gradeSheets.isEmpty()) {
            return List.of();
        }

        List<Long> gradeSheetIds = gradeSheets.stream()
                .map(GradeSheet::getId)
                .toList();

        Map<Long, CourseClass> courseClassMap = courseClassMapper
                .selectBatchIds(courseClassIds)
                .stream()
                .collect(Collectors.toMap(
                        CourseClass::getId,
                        Function.identity()
                ));

        List<Long> courseIds = courseClassMap.values()
                .stream()
                .map(CourseClass::getCourseId)
                .distinct()
                .toList();

        List<Long> semesterIds = courseClassMap.values()
                .stream()
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

        Map<Long, Score> scoreMap = scoreMapper.selectList(
                        Wrappers.<Score>lambdaQuery()
                                .in(Score::getGradeSheetId, gradeSheetIds)
                                .eq(Score::getStudentId, student.getId())
                )
                .stream()
                .collect(Collectors.toMap(
                        Score::getGradeSheetId,
                        Function.identity()
                ));

        return gradeSheets.stream()
                .map(gradeSheet -> {
                    CourseClass courseClass = courseClassMap.get(
                            gradeSheet.getCourseClassId()
                    );

                    if (courseClass == null) {
                        return null;
                    }

                    Course course = courseMap.get(
                            courseClass.getCourseId()
                    );
                    Semester semester = semesterMap.get(
                            courseClass.getSemesterId()
                    );
                    Score score = scoreMap.get(gradeSheet.getId());

                    return new StudentGradeResponse(
                            course == null
                                    ? ""
                                    : course.getCourseCode(),
                            course == null
                                    ? ""
                                    : course.getCourseName(),
                            semester == null
                                    ? ""
                                    : semester.getSemesterName(),
                            courseClass.getClassName(),
                            course == null ? null : course.getCredit(),
                            score == null
                                    ? null
                                    : score.getUsualScore(),
                            score == null
                                    ? null
                                    : score.getExamScore(),
                            score == null
                                    ? null
                                    : score.getFinalScore(),
                            gradeSheet.getPublishedAt()
                    );
                })
                .filter(item -> item != null)
                .sorted(
                        Comparator.comparing(
                                StudentGradeResponse::getPublishedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .toList();
    }
}