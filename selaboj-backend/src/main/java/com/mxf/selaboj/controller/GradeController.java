package com.mxf.selaboj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mxf.selaboj.common.BaseResponse;
import com.mxf.selaboj.common.ErrorCode;
import com.mxf.selaboj.common.ResultUtils;
import com.mxf.selaboj.exception.BusinessException;
import com.mxf.selaboj.mapper.ClassStudentMapper;
import com.mxf.selaboj.mapper.ExamMapper;
import com.mxf.selaboj.mapper.ExamRecordMapper;
import com.mxf.selaboj.mapper.UserMapper;
import com.mxf.selaboj.model.entity.ClassStudent;
import com.mxf.selaboj.model.entity.Exam;
import com.mxf.selaboj.model.entity.ExamRecord;
import com.mxf.selaboj.model.entity.User;
import com.mxf.selaboj.model.vo.ExamRecordVO;
import com.mxf.selaboj.service.ExamService;
import com.mxf.selaboj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/grade")
@Slf4j
public class GradeController {

    @Resource
    private ExamService examService;

    @Resource
    private UserService userService;

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private ExamMapper examMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    /**
     * 获取所有考试列表（用于选择考试）
     */
    @GetMapping("/exam/list")
    public BaseResponse<List<Exam>> getExamList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以查询成绩");
        }
        
        List<Exam> exams = examMapper.selectList(null);
        return ResultUtils.success(exams);
    }

    /**
     * 根据考试ID查询所有学生成绩
     */
    @GetMapping("/exam/{examId}")
    public BaseResponse<Page<ExamRecordVO>> getExamGrades(@PathVariable Long examId, 
                                                          @RequestParam(defaultValue = "1") Integer current,
                                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                                          HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以查询成绩");
        }

        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "考试不存在");
        }

        QueryWrapper<ExamRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("examId", examId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderByDesc("totalScore");

        Page<ExamRecord> examRecordPage = examRecordMapper.selectPage(new Page<>(current, pageSize), queryWrapper);
        
        List<ExamRecordVO> voList = examRecordPage.getRecords().stream().map(record -> {
            ExamRecordVO vo = ExamRecordVO.objToVo(record);
            vo.setExamName(exam.getExamName());
            
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
            }
            
            return vo;
        }).collect(Collectors.toList());

        Page<ExamRecordVO> resultPage = new Page<>(current, pageSize, examRecordPage.getTotal());
        resultPage.setRecords(voList);

        return ResultUtils.success(resultPage);
    }

    /**
     * 获取单个学生的考试记录
     */
    @GetMapping("/record/{recordId}")
    public BaseResponse<ExamRecordVO> getExamRecord(@PathVariable Long recordId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以查询成绩");
        }

        return ResultUtils.success(examService.getExamRecord(recordId, request));
    }

    /**
     * 根据班级ID查询所有成员的所有考试成绩
     */
    @GetMapping("/class/{classId}")
    public BaseResponse<Page<ExamRecordVO>> getClassGrades(@PathVariable Long classId,
                                                            @RequestParam(defaultValue = "1") Integer current,
                                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以查询成绩");
        }

        // 获取班级所有成员ID
        QueryWrapper<ClassStudent> classStudentQueryWrapper = new QueryWrapper<>();
        classStudentQueryWrapper.eq("classId", classId);
        classStudentQueryWrapper.eq("isDelete", false);
        List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQueryWrapper);

        if (classStudents == null || classStudents.isEmpty()) {
            Page<ExamRecordVO> emptyPage = new Page<>(current, pageSize, 0);
            emptyPage.setRecords(new ArrayList<>());
            return ResultUtils.success(emptyPage);
        }

        List<Long> userIds = classStudents.stream()
                .map(ClassStudent::getUserId)
                .collect(Collectors.toList());

        // 查询这些用户的所有考试记录
        QueryWrapper<ExamRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("userId", userIds);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderByDesc("createTime");

        Page<ExamRecord> examRecordPage = examRecordMapper.selectPage(new Page<>(current, pageSize), queryWrapper);

        List<ExamRecordVO> voList = examRecordPage.getRecords().stream().map(record -> {
            ExamRecordVO vo = ExamRecordVO.objToVo(record);

            Exam exam = examMapper.selectById(record.getExamId());
            if (exam != null) {
                vo.setExamName(exam.getExamName());
            }

            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
            }

            return vo;
        }).collect(Collectors.toList());

        Page<ExamRecordVO> resultPage = new Page<>(current, pageSize, examRecordPage.getTotal());
        resultPage.setRecords(voList);

        return ResultUtils.success(resultPage);
    }
}