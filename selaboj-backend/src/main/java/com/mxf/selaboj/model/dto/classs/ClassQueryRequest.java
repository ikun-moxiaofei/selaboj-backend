package com.mxf.selaboj.model.dto.classs;

import com.mxf.selaboj.common.PageRequest;
import lombok.Data;

/**
 * 班级查询请求
 */
@Data
public class ClassQueryRequest extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 班级代码
     */
    private String classCode;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序
     */
    private String sortOrder;
}

