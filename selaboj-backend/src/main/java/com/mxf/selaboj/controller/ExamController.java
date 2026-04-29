package com.mxf.selaboj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mxf.selaboj.common.BaseResponse;
import com.mxf.selaboj.common.DeleteRequest;
import com.mxf.selaboj.common.ErrorCode;
import com.mxf.selaboj.common.ResultUtils;
import com.mxf.selaboj.exception.BusinessException;
import com.mxf.selaboj.exception.ThrowUtils;
import com.mxf.selaboj.model.dto.exam.ExamAddRequest;
import com.mxf.selaboj.model.dto.exam.ExamEditRequest;
import com.mxf.selaboj.model.dto.exam.ExamQueryRequest;
import com.mxf.selaboj.model.entity.Exam;
import com.mxf.selaboj.model.vo.ExamRecordVO;
import com.mxf.selaboj.model.vo.ExamVO;
import com.mxf.selaboj.model.entity.User;
import com.mxf.selaboj.service.ExamService;
import com.mxf.selaboj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 考试控制器
 */
@RestController
@RequestMapping("/exam")
@Slf4j
public class ExamController {

    @Resource
    private ExamService examService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建考试
     *
     * @param examAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addExam(@RequestBody ExamAddRequest examAddRequest, HttpServletRequest request) {
        if (examAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查是否为老师或管理员
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "只有老师可以创建考试");
        }
        long newExamId = examService.addExam(examAddRequest, request);
        return ResultUtils.success(newExamId);
    }

    /**
     * 删除考试
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteExam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "只有老师可以删除考试");
        }
        long id = deleteRequest.getId();
        Exam oldExam = examService.getById(id);
        ThrowUtils.throwIf(oldExam == null, ErrorCode.NOT_FOUND_ERROR);
        boolean b = examService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新考试
     *
     * @param examEditRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateExam(@RequestBody ExamEditRequest examEditRequest, HttpServletRequest request) {
        if (examEditRequest == null || examEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = examService.updateExam(examEditRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取考试
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Exam> getExamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Exam exam = examService.getById(id);
        if (exam == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(exam);
    }

    /**
     * 根据 id 获取考试（脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ExamVO> getExamVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Exam exam = examService.getById(id);
        if (exam == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(examService.getExamVO(exam, request));
    }

    /**
     * 分页获取考试列表（封装类）
     *
     * @param examQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ExamVO>> listExamVOByPage(@RequestBody ExamQueryRequest examQueryRequest,
                                                       HttpServletRequest request) {
        long current = examQueryRequest.getCurrent();
        long size = examQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Exam> examPage = examService.page(new Page<>(current, size),
                examService.getQueryWrapper(examQueryRequest));
        return ResultUtils.success(examService.getExamVOPage(examPage, request));
    }

    // endregion

    /**
     * 发布考试到班级
     *
     * @param examId
     * @param classId
     * @return
     */
    @PostMapping("/publish")
    public BaseResponse<Boolean> publishExamToClass(@RequestParam long examId, @RequestParam long classId) {
        boolean result = examService.publishExamToClass(examId, classId);
        return ResultUtils.success(result);
    }

    /**
     * 开始考试
     *
     * @param examId
     * @param request
     * @return
     */
    @PostMapping("/start")
    public BaseResponse<Long> startExam(@RequestParam long examId, HttpServletRequest request) {
        long examRecordId = examService.startExam(examId, request);
        return ResultUtils.success(examRecordId);
    }

    /**
     * 提交答案
     *
     * @param examRecordId
     * @param questionId
     * @param answer
     * @param request
     * @return
     */
    @PostMapping("/submit")
    public BaseResponse<Boolean> submitAnswer(@RequestParam long examRecordId, @RequestParam long questionId, 
                                             @RequestParam String answer, HttpServletRequest request) {
        boolean result = examService.submitAnswer(examRecordId, questionId, answer, request);
        return ResultUtils.success(result);
    }

    /**
     * 结束考试
     *
     * @param examRecordId
     * @param request
     * @return
     */
    @PostMapping("/end")
    public BaseResponse<Boolean> endExam(@RequestParam long examRecordId, HttpServletRequest request) {
        boolean result = examService.endExam(examRecordId, request);
        return ResultUtils.success(result);
    }

    /**
     * 获取考试记录
     *
     * @param examRecordId
     * @param request
     * @return
     */
    @GetMapping("/record/get")
    public BaseResponse<ExamRecordVO> getExamRecord(@RequestParam long examRecordId, HttpServletRequest request) {
        ExamRecordVO examRecordVO = examService.getExamRecord(examRecordId, request);
        return ResultUtils.success(examRecordVO);
    }

    /**
     * 获取用户考试记录列表
     *
     * @param examId
     * @param request
     * @return
     */
    @GetMapping("/record/list")
    public BaseResponse<List<ExamRecordVO>> getUserExamRecords(@RequestParam long examId, HttpServletRequest request) {
        List<ExamRecordVO> examRecordVOs = examService.getUserExamRecords(examId, request);
        return ResultUtils.success(examRecordVOs);
    }

    /**
     * 自动评分
     *
     * @param examRecordId
     * @return
     */
    @PostMapping("/grade")
    public BaseResponse<Boolean> autoGrade(@RequestParam long examRecordId) {
        boolean result = examService.autoGrade(examRecordId);
        return ResultUtils.success(result);
    }
}
