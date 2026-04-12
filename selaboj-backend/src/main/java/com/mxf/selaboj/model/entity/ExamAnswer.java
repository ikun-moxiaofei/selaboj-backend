package com.mxf.selaboj.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 考试答题记录
 * @TableName exam_answer
 */
@TableName(value ="exam_answer")
@Data
public class ExamAnswer implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 考试记录 id
     */
    private Long examRecordId;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 答案
     */
    private String answer;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 状态（0 - 未作答，1 - 已作答，2 - 正确，3 - 错误）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
