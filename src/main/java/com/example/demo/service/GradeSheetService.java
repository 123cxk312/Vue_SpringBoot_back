package com.example.demo.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.ContextUtil;
import com.example.demo.dto.grade.GradeOperationLogResponse;
import com.example.demo.dto.grade.GradeSheetDetailResponse;
import com.example.demo.dto.grade.ReviewRequest;
import com.example.demo.dto.grade.ScoreItemRequest;
import com.example.demo.dto.grade.ScoreRowResponse;
import com.example.demo.dto.grade.ScoreSaveRequest;
import com.example.demo.entity.CourseClass;
import com.example.demo.entity.CourseClassStudent;
import com.example.demo.entity.GradeOperationLog;
import com.example.demo.entity.GradeSheet;
import com.example.demo.entity.Score;
import com.example.demo.entity.Student;
import com.example.demo.entity.SysUser;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.GradeSheetStatus;
import com.example.demo.enums.RoleEnum;
import com.example.demo.exception.AuthException;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CourseClassMapper;
import com.example.demo.mapper.CourseClassStudentMapper;
import com.example.demo.mapper.GradeOperationLogMapper;
import com.example.demo.mapper.GradeSheetMapper;
import com.example.demo.mapper.ScoreMapper;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.mapper.TeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GradeSheetService {

    private final GradeSheetMapper gradeSheetMapper;
    private final ScoreMapper scoreMapper;
    private final GradeOperationLogMapper logMapper;
    private final CourseClassMapper courseClassMapper;
    private final CourseClassStudentMapper relationMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper sysUserMapper;
    private final TeacherMapper teacherMapper;

    public GradeSheetService(
            GradeSheetMapper gradeSheetMapper,
            ScoreMapper scoreMapper,
            GradeOperationLogMapper logMapper,
            CourseClassMapper courseClassMapper,
            CourseClassStudentMapper relationMapper,
            StudentMapper studentMapper,
            SysUserMapper sysUserMapper,
            TeacherMapper teacherMapper
    ) {
        this.gradeSheetMapper = gradeSheetMapper;
        this.scoreMapper = scoreMapper;
        this.logMapper = logMapper;
        this.courseClassMapper = courseClassMapper;
        this.relationMapper = relationMapper;
        this.studentMapper = studentMapper;
        this.sysUserMapper = sysUserMapper;
        this.teacherMapper = teacherMapper;
    }

    @Transactional
    public GradeSheetDetailResponse detailByCourseClass(
            String courseClassId
    ) {
        Long id = parseId(courseClassId);
        CourseClass courseClass = getCourseClass(id);

        authorizeCourseClass(courseClass);
        GradeSheet gradeSheet = getOrCreateGradeSheet(id);

        List<ScoreRowResponse> rows = buildRows(gradeSheet.getId());

        return new GradeSheetDetailResponse(
                String.valueOf(gradeSheet.getId()),
                String.valueOf(courseClass.getId()),
                gradeSheet.getStatus(),
                courseClass.getUsualWeight(),
                courseClass.getExamWeight(),
                rows
        );
    }

    @Transactional
    public GradeSheetDetailResponse saveScores(
            String id,
            ScoreSaveRequest request
    ) {
        Long gradeSheetId = parseId(id);
        GradeSheet gradeSheet = getGradeSheet(gradeSheetId);
        CourseClass courseClass =
                getCourseClass(gradeSheet.getCourseClassId());

        authorizeCourseClass(courseClass);
        requireEditable(gradeSheet);

        Map<Long, ScoreItemRequest> requestItems =
                normalizeScoreItems(courseClass, request);

        Map<Long, Score> existingScores = scoreMapper.selectList(
                        Wrappers.<Score>lambdaQuery()
                                .eq(
                                        Score::getGradeSheetId,
                                        gradeSheetId
                                )
                )
                .stream()
                .collect(Collectors.toMap(
                        Score::getStudentId,
                        Function.identity()
                ));

        for (Map.Entry<Long, ScoreItemRequest> entry
                : requestItems.entrySet()) {
            Long studentId = entry.getKey();
            ScoreItemRequest item = entry.getValue();
            Score score = existingScores.get(studentId);
            boolean create = score == null;

            if (create) {
                score = new Score();
                score.setGradeSheetId(gradeSheetId);
                score.setStudentId(studentId);
                score.setCreatedAt(LocalDateTime.now());
            }

            score.setUsualScore(item.getUsualScore());
            score.setExamScore(item.getExamScore());
            score.setFinalScore(calculateFinalScore(
                    item.getUsualScore(),
                    item.getExamScore(),
                    courseClass
            ));
            score.setUpdatedAt(LocalDateTime.now());

            if (create) {
                scoreMapper.insert(score);
            } else {
                scoreMapper.updateById(score);
            }
        }

        return detailByCourseClass(
                String.valueOf(courseClass.getId())
        );
    }

    @Transactional
    public void submit(String id) {
        Long gradeSheetId = parseId(id);
        GradeSheet gradeSheet = getGradeSheet(gradeSheetId);
        CourseClass courseClass =
                getCourseClass(gradeSheet.getCourseClassId());

        Teacher teacher = requireCurrentTeacher(courseClass);

        requireStatus(
                gradeSheet,
                GradeSheetStatus.DRAFT,
                GradeSheetStatus.RETURNED
        );
        validateAndRecalculateScores(
                gradeSheet.getId(),
                courseClass
        );

        String beforeStatus = gradeSheet.getStatus();
        LocalDateTime now = LocalDateTime.now();

        gradeSheet.setStatus(GradeSheetStatus.SUBMITTED.getCode());
        gradeSheet.setSubmittedBy(teacher.getUserId());
        gradeSheet.setSubmittedAt(now);
        gradeSheet.setReviewedBy(null);
        gradeSheet.setReviewedAt(null);
        gradeSheet.setPublishedAt(null);
        gradeSheet.setReviewComment(null);

        gradeSheetMapper.updateById(gradeSheet);

        addLog(
                gradeSheet.getId(),
                teacher.getUserId(),
                "SUBMIT",
                beforeStatus,
                GradeSheetStatus.SUBMITTED.getCode(),
                null
        );
    }

    @Transactional
    public void review(String id) {
        GradeSheet gradeSheet = requireAdminGradeSheet(id);
        requireStatus(gradeSheet, GradeSheetStatus.SUBMITTED);

        String beforeStatus = gradeSheet.getStatus();
        LocalDateTime now = LocalDateTime.now();
        Long operatorId = ContextUtil.getUserId();

        gradeSheet.setStatus(GradeSheetStatus.REVIEWED.getCode());
        gradeSheet.setReviewedBy(operatorId);
        gradeSheet.setReviewedAt(now);
        gradeSheet.setReviewComment(null);

        gradeSheetMapper.updateById(gradeSheet);

        addLog(
                gradeSheet.getId(),
                operatorId,
                "REVIEW",
                beforeStatus,
                GradeSheetStatus.REVIEWED.getCode(),
                null
        );
    }

    @Transactional
    public void returnSheet(String id, ReviewRequest request) {
        GradeSheet gradeSheet = requireAdminGradeSheet(id);
        requireStatus(gradeSheet, GradeSheetStatus.SUBMITTED);

        String beforeStatus = gradeSheet.getStatus();
        LocalDateTime now = LocalDateTime.now();
        Long operatorId = ContextUtil.getUserId();
        String comment = request.getComment().trim();

        gradeSheet.setStatus(GradeSheetStatus.RETURNED.getCode());
        gradeSheet.setReviewedBy(operatorId);
        gradeSheet.setReviewedAt(now);
        gradeSheet.setReviewComment(comment);

        gradeSheetMapper.updateById(gradeSheet);

        addLog(
                gradeSheet.getId(),
                operatorId,
                "RETURN",
                beforeStatus,
                GradeSheetStatus.RETURNED.getCode(),
                comment
        );
    }

    @Transactional
    public void publish(String id) {
        GradeSheet gradeSheet = requireAdminGradeSheet(id);
        requireStatus(gradeSheet, GradeSheetStatus.REVIEWED);

        String beforeStatus = gradeSheet.getStatus();
        Long operatorId = ContextUtil.getUserId();

        gradeSheet.setStatus(GradeSheetStatus.PUBLISHED.getCode());
        gradeSheet.setPublishedAt(LocalDateTime.now());

        gradeSheetMapper.updateById(gradeSheet);

        addLog(
                gradeSheet.getId(),
                operatorId,
                "PUBLISH",
                beforeStatus,
                GradeSheetStatus.PUBLISHED.getCode(),
                null
        );
    }

    public List<GradeOperationLogResponse> logs(String id) {
        Long gradeSheetId = parseId(id);
        GradeSheet gradeSheet = getGradeSheet(gradeSheetId);
        CourseClass courseClass =
                getCourseClass(gradeSheet.getCourseClassId());

        authorizeCourseClass(courseClass);

        return logMapper.selectList(
                        Wrappers.<GradeOperationLog>lambdaQuery()
                                .eq(
                                        GradeOperationLog::getGradeSheetId,
                                        gradeSheetId
                                )
                                .orderByAsc(
                                        GradeOperationLog::getCreatedAt
                                )
                                .orderByAsc(GradeOperationLog::getId)
                )
                .stream()
                .map(log -> new GradeOperationLogResponse(
                        String.valueOf(log.getId()),
                        String.valueOf(log.getGradeSheetId()),
                        String.valueOf(log.getOperatorId()),
                        log.getAction(),
                        log.getBeforeStatus(),
                        log.getAfterStatus(),
                        log.getRemark(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    private List<ScoreRowResponse> buildRows(Long gradeSheetId) {
        GradeSheet gradeSheet = gradeSheetMapper.selectById(gradeSheetId);

        if (gradeSheet == null) {
            throw new BusinessException(404, "成绩单不存在");
        }

        List<CourseClassStudent> relations = relationMapper.selectList(
                Wrappers.<CourseClassStudent>lambdaQuery()
                        .eq(
                                CourseClassStudent::getCourseClassId,
                                gradeSheet.getCourseClassId()
                        )
        );

        if (relations.isEmpty()) {
            return List.of();
        }

        List<Long> studentIds = relations.stream()
                .map(CourseClassStudent::getStudentId)
                .toList();

        List<Student> students = studentMapper.selectBatchIds(studentIds);

        if (students.isEmpty()) {
            return List.of();
        }

        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(
                        Student::getId,
                        Function.identity()
                ));

        List<Long> userIds = students.stream()
                .map(Student::getUserId)
                .distinct()
                .toList();

        Map<Long, SysUser> userMap = sysUserMapper
                .selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        Function.identity()
                ));

        Map<Long, Score> scoreMap = scoreMapper.selectList(
                        Wrappers.<Score>lambdaQuery()
                                .eq(
                                        Score::getGradeSheetId,
                                        gradeSheetId
                                )
                )
                .stream()
                .collect(Collectors.toMap(
                        Score::getStudentId,
                        Function.identity()
                ));

        List<ScoreRowResponse> rows = new ArrayList<>();

        for (CourseClassStudent relation : relations) {
            Student student = studentMap.get(relation.getStudentId());

            if (student == null) {
                continue;
            }

            SysUser user = userMap.get(student.getUserId());
            Score score = scoreMap.get(student.getId());

            rows.add(new ScoreRowResponse(
                    String.valueOf(student.getId()),
                    student.getStudentNo(),
                    user == null ? "未知学生" : user.getRealName(),
                    score == null ? null : score.getUsualScore(),
                    score == null ? null : score.getExamScore(),
                    score == null ? null : score.getFinalScore()
            ));
        }

        rows.sort(
                Comparator.comparing(
                        ScoreRowResponse::getStudentNo,
                        Comparator.nullsLast(String::compareTo)
                )
        );

        return rows;
    }

    private void requireEditable(GradeSheet gradeSheet) {
        GradeSheetStatus status =
                GradeSheetStatus.fromCode(gradeSheet.getStatus());

        if (status != GradeSheetStatus.DRAFT
                && status != GradeSheetStatus.RETURNED) {
            throw new BusinessException(
                    409,
                    "当前成绩单状态不允许修改"
            );
        }
    }

    private Map<Long, ScoreItemRequest> normalizeScoreItems(
            CourseClass courseClass,
            ScoreSaveRequest request
    ) {
        Set<Long> allowedStudentIds = relationMapper.selectList(
                        Wrappers.<CourseClassStudent>lambdaQuery()
                                .eq(
                                        CourseClassStudent::getCourseClassId,
                                        courseClass.getId()
                                )
                )
                .stream()
                .map(CourseClassStudent::getStudentId)
                .collect(Collectors.toSet());

        Map<Long, ScoreItemRequest> result = new LinkedHashMap<>();

        for (ScoreItemRequest item : request.getScores()) {
            Long studentId = parseId(item.getStudentId());

            if (!allowedStudentIds.contains(studentId)) {
                throw new BusinessException(
                        "学生不属于当前教学班"
                );
            }

            if (result.put(studentId, item) != null) {
                throw new BusinessException(
                        "同一学生不能重复提交成绩"
                );
            }
        }

        return result;
    }

    private BigDecimal calculateFinalScore(
            BigDecimal usualScore,
            BigDecimal examScore,
            CourseClass courseClass
    ) {
        if (usualScore == null || examScore == null) {
            return null;
        }

        return usualScore
                .multiply(courseClass.getUsualWeight())
                .add(
                        examScore.multiply(
                                courseClass.getExamWeight()
                        )
                )
                .setScale(2, RoundingMode.HALF_UP);
    }

    private GradeSheet requireAdminGradeSheet(String id) {
        ContextUtil.requireRole(RoleEnum.ADMIN);

        GradeSheet gradeSheet = getGradeSheet(parseId(id));
        CourseClass courseClass =
                getCourseClass(gradeSheet.getCourseClassId());

        authorizeCourseClass(courseClass);
        return gradeSheet;
    }

    private Teacher requireCurrentTeacher(CourseClass courseClass) {
        ContextUtil.requireRole(RoleEnum.TEACHER);

        Long userId = ContextUtil.getUserId();

        Teacher teacher = teacherMapper.selectOne(
                Wrappers.<Teacher>lambdaQuery()
                        .eq(Teacher::getUserId, userId)
        );

        if (teacher == null
                || !teacher.getId().equals(courseClass.getTeacherId())) {
            throw new AuthException(
                    403,
                    "没有权限操作该教学班"
            );
        }

        return teacher;
    }

    private void requireStatus(
            GradeSheet gradeSheet,
            GradeSheetStatus... allowedStatuses
    ) {
        GradeSheetStatus current =
                GradeSheetStatus.fromCode(gradeSheet.getStatus());

        for (GradeSheetStatus allowed : allowedStatuses) {
            if (current == allowed) {
                return;
            }
        }

        throw new BusinessException(
                409,
                "当前成绩单状态不允许执行该操作"
        );
    }

    private void validateAndRecalculateScores(
            Long gradeSheetId,
            CourseClass courseClass
    ) {
        List<Long> studentIds = relationMapper.selectList(
                        Wrappers.<CourseClassStudent>lambdaQuery()
                                .eq(
                                        CourseClassStudent::getCourseClassId,
                                        courseClass.getId()
                                )
                )
                .stream()
                .map(CourseClassStudent::getStudentId)
                .toList();

        if (studentIds.isEmpty()) {
            throw new BusinessException(
                    "教学班没有学生，不能提交成绩单"
            );
        }

        Map<Long, Score> scoreMap = scoreMapper.selectList(
                        Wrappers.<Score>lambdaQuery()
                                .eq(Score::getGradeSheetId, gradeSheetId)
                )
                .stream()
                .collect(Collectors.toMap(
                        Score::getStudentId,
                        Function.identity()
                ));

        for (Long studentId : studentIds) {
            Score score = scoreMap.get(studentId);

            if (score == null
                    || score.getUsualScore() == null
                    || score.getExamScore() == null) {
                throw new BusinessException(
                        "存在未填写完整的成绩，不能提交"
                );
            }
        }

        LocalDateTime now = LocalDateTime.now();

        for (Long studentId : studentIds) {
            Score score = scoreMap.get(studentId);
            score.setFinalScore(calculateFinalScore(
                    score.getUsualScore(),
                    score.getExamScore(),
                    courseClass
            ));
            score.setUpdatedAt(now);
            scoreMapper.updateById(score);
        }
    }

    private void addLog(
            Long gradeSheetId,
            Long operatorId,
            String action,
            String beforeStatus,
            String afterStatus,
            String remark
    ) {
        GradeOperationLog log = new GradeOperationLog();
        log.setGradeSheetId(gradeSheetId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());

        logMapper.insert(log);
    }

    private void authorizeCourseClass(CourseClass courseClass) {
        RoleEnum role = ContextUtil.getRole();

        if (role == RoleEnum.ADMIN) {
            return;
        }

        if (role == RoleEnum.TEACHER) {
            Long userId = ContextUtil.getUserId();

            Teacher teacher = teacherMapper.selectOne(
                    Wrappers.<Teacher>lambdaQuery()
                            .eq(Teacher::getUserId, userId)
            );

            if (teacher == null
                    || !teacher.getId().equals(courseClass.getTeacherId())) {
                throw new AuthException(
                        403,
                        "没有权限访问该教学班"
                );
            }

            return;
        }

        throw new AuthException(403, "没有权限访问该教学班");
    }

    private GradeSheet getOrCreateGradeSheet(Long courseClassId) {
        GradeSheet gradeSheet = gradeSheetMapper.selectOne(
                Wrappers.<GradeSheet>lambdaQuery()
                        .eq(
                                GradeSheet::getCourseClassId,
                                courseClassId
                        )
        );

        if (gradeSheet != null) {
            return gradeSheet;
        }

        GradeSheet newGradeSheet = new GradeSheet();
        newGradeSheet.setCourseClassId(courseClassId);
        newGradeSheet.setStatus(GradeSheetStatus.DRAFT.getCode());

        gradeSheetMapper.insert(newGradeSheet);
        return newGradeSheet;
    }

    private GradeSheet getGradeSheet(Long id) {
        GradeSheet gradeSheet = gradeSheetMapper.selectById(id);

        if (gradeSheet == null) {
            throw new BusinessException(404, "成绩单不存在");
        }

        return gradeSheet;
    }

    private CourseClass getCourseClass(Long id) {
        CourseClass courseClass = courseClassMapper.selectById(id);

        if (courseClass == null) {
            throw new BusinessException(404, "教学班不存在");
        }

        return courseClass;
    }

    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException("ID 格式错误");
        }
    }
}
