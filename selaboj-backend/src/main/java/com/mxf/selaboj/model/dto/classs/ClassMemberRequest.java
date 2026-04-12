package com.mxf.selaboj.model.dto.classs;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 班级成员管理请求
 */
@Data
public class ClassMemberRequest implements Serializable {
    /**
     * 班级id
     */
    private Long classId;

    /**
     * 用户id列表
     */
    private List<Long> userIds;

    private static final long serialVersionUID = 1L;
}
