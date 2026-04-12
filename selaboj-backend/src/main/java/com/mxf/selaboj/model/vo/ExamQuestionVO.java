package com.mxf.selaboj.model.vo;

import com.mxf.selaboj.model.dto.question.QuestionOption;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 考试题目视图
 */
@Data
public class ExamQuestionVO implements Serializable {
    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 题目标题
     */
    private String questionTitle;

    /**
     * 题目类型
     */
    private Integer questionType;

    /**
     * 题目分值
     */
    private Integer score;

    /**
     * 选择题选项
     */
    private List<QuestionOption> options;

    /**
     * 题目信息
     */
    private QuestionVO question;

    private static final long serialVersionUID = 1L;
}
