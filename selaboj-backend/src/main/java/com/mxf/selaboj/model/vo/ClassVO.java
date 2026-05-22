package com.mxf.selaboj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mxf.selaboj.model.entity.Class;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 班级视图
 */
@Data
public class ClassVO implements Serializable {
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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 包装类转对象
     *
     * @param classVO
     * @return
     */
    public static Class voToObj(ClassVO classVO) {
        if (classVO == null) {
            return null;
        }
        Class clazz = new Class();
        BeanUtils.copyProperties(classVO, clazz);
        return clazz;
    }

    /**
     * 对象转包装类
     *
     * @param clazz
     * @return
     */
    public static ClassVO objToVo(Class clazz) {
        if (clazz == null) {
            return null;
        }
        ClassVO classVO = new ClassVO();
        BeanUtils.copyProperties(clazz, classVO);
        return classVO;
    }

    private static final long serialVersionUID = 1L;
}
