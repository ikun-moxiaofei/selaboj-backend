package com.mxf.selaboj.model.dto.question;

import lombok.Data;

/**
 * 选择题选项
 */
@Data
public class QuestionOption {
    /**
     * 选项标识（A、B、C、D等）
     */
    private String key;

    /**
     * 选项内容
     */
    private String value;
}