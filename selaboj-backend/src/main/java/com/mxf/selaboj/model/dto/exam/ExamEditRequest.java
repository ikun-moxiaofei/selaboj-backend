package com.mxf.selaboj.model.dto.exam;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 考试编辑请求
 */
@Data
public class ExamEditRequest implements Serializable {
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
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 题目列表
     */
    private List<ExamQuestionRequest> questions;

    private static final long serialVersionUID = 1L;
}
