package com.mxf.selaboj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mxf.selaboj.model.entity.Exam;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 考试视图
 */
@Data
public class ExamVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 考试描述
     */
    private String description;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 题目列表
     */
    private List<ExamQuestionVO> questions;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 包装类转对象
     *
     * @param examVO
     * @return
     */
    public static Exam voToObj(ExamVO examVO) {
        if (examVO == null) {
            return null;
        }
        Exam exam = new Exam();
        BeanUtils.copyProperties(examVO, exam);
        return exam;
    }

    /**
     * 对象转包装类
     *
     * @param exam
     * @return
     */
    public static ExamVO objToVo(Exam exam) {
        if (exam == null) {
            return null;
        }
        ExamVO examVO = new ExamVO();
        BeanUtils.copyProperties(exam, examVO);
        return examVO;
    }

    private static final long serialVersionUID = 1L;
}
