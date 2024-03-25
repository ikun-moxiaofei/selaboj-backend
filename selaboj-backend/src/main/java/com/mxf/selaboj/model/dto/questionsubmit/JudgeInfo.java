package com.mxf.selaboj.model.dto.questionsubmit;

import lombok.Data;

/**
 * 判题消息
 */
@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 时间
     */
    private long time;

    /**
     * 空间
     */
    private long memory;
}
