package com.mxf.selaboj.model.dto.exam;

import com.mxf.selaboj.common.PageRequest;
import lombok.Data;

import java.util.Date;

/**
 * 考试查询请求
 */
@Data
public class ExamQueryRequest extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序
     */
    private String sortOrder;
}
