package com.mxf.selaboj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mxf.selaboj.model.dto.exam.ExamAddRequest;
import com.mxf.selaboj.model.dto.exam.ExamEditRequest;
import com.mxf.selaboj.model.dto.exam.ExamQueryRequest;
import com.mxf.selaboj.model.entity.Exam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mxf.selaboj.model.vo.ExamVO;
import com.mxf.selaboj.model.vo.ExamRecordVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 考试服务
 */
public interface ExamService extends IService<Exam> {
    /**
     * 校验
     *
     * @param exam
     * @param add
     */
    void validExam(Exam exam, boolean add);

    /**
     * 获取查询条件
     *
     * @param examQueryRequest
     * @return
     */
    QueryWrapper<Exam> getQueryWrapper(ExamQueryRequest examQueryRequest);

    /**
     * 获取考试封装
     *
     * @param exam
     * @param request
     * @return
     */
    ExamVO getExamVO(Exam exam, HttpServletRequest request);

    /**
     * 分页获取考试封装
     *
     * @param examPage
     * @param request
     * @return
     */
    Page<ExamVO> getExamVOPage(Page<Exam> examPage, HttpServletRequest request);

    /**
     * 创建考试
     * @param examAddRequest
     * @param request
     * @return
     */
    long addExam(ExamAddRequest examAddRequest, HttpServletRequest request);

    /**
     * 更新考试
     * @param examEditRequest
     * @param request
     * @return
     */
    boolean updateExam(ExamEditRequest examEditRequest, HttpServletRequest request);

    /**
     * 发布考试到班级
     * @param examId
     * @param classId
     * @return
     */
    boolean publishExamToClass(long examId, long classId);

    /**
     * 开始考试
     * @param examId
     * @param request
     * @return
     */
    long startExam(long examId, HttpServletRequest request);

    /**
     * 提交答案
     * @param examRecordId
     * @param questionId
     * @param answer
     * @param request
     * @return
     */
    boolean submitAnswer(long examRecordId, long questionId, String answer, HttpServletRequest request);

    /**
     * 结束考试
     * @param examRecordId
     * @param request
     * @return
     */
    boolean endExam(long examRecordId, HttpServletRequest request);

    /**
     * 获取考试记录
     * @param examRecordId
     * @param request
     * @return
     */
    ExamRecordVO getExamRecord(long examRecordId, HttpServletRequest request);

    /**
     * 获取用户考试记录列表
     * @param examId
     * @param request
     * @return
     */
    List<ExamRecordVO> getUserExamRecords(long examId, HttpServletRequest request);

    /**
     * 自动评分
     * @param examRecordId
     * @return
     */
    boolean autoGrade(long examRecordId);

    boolean removeById(Long id);
}
