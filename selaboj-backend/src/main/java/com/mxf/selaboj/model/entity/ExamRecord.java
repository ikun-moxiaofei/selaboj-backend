package com.mxf.selaboj.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 考试记录
 * @TableName exam_record
 */
@TableName(value ="exam_record")
@Data
public class ExamRecord implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 考试 id
     */
    private Long examId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 状态（0 - 待考试，1 - 考试中，2 - 已完成）
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

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
