package com.mxf.selaboj.model.dto.classs;

import lombok.Data;

import java.io.Serializable;

/**
 * 班级编辑请求
 */
@Data
public class ClassEditRequest implements Serializable {
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
     * 班级描述
     */
    private String description;

    private static final long serialVersionUID = 1L;
}
