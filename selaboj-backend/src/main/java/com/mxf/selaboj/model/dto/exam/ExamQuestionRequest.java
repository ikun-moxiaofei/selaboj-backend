package com.mxf.selaboj.model.dto.exam;

import lombok.Data;

import java.io.Serializable;

/**
 * 考试题目请求
 */
@Data
public class ExamQuestionRequest implements Serializable {
    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 题目分值
     */
    private Integer score;

    private static final long serialVersionUID = 1L;
}
