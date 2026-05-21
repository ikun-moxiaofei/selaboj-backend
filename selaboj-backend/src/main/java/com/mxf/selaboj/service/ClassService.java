package com.mxf.selaboj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mxf.selaboj.model.dto.classs.ClassQueryRequest;
import com.mxf.selaboj.model.entity.Class;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mxf.selaboj.model.vo.ClassVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 班级服务
 */
public interface ClassService extends IService<Class> {
    /**
     * 校验
     *
     * @param clazz
     * @param add
     */
    void validClass(Class clazz, boolean add);

    /**
     * 获取查询条件
     *
     * @param classQueryRequest
     * @return
     */
    QueryWrapper<Class> getQueryWrapper(ClassQueryRequest classQueryRequest);

    /**
     * 获取班级封装
     *
     * @param clazz
     * @param request
     * @return
     */
    ClassVO getClassVO(Class clazz, HttpServletRequest request);

    /**
     * 分页获取班级封装
     *
     * @param classPage
     * @param request
     * @return
     */
    Page<ClassVO> getClassVOPage(Page<Class> classPage, HttpServletRequest request);

    /**
     * 添加班级成员
     * @param classId
     * @param userIds
     * @return
     */
    boolean addClassMembers(Long classId, java.util.List<Long> userIds);

    /**
     * 移除班级成员
     * @param classId
     * @param userIds
     * @return
     */
    boolean removeClassMembers(Long classId, java.util.List<Long> userIds);

    /**
     * 获取班级成员列表
     * @param classId
     * @return
     */
    java.util.List<com.mxf.selaboj.model.vo.UserVO> getClassMembers(Long classId);

    /**
     * 学生加入班级
     * @param classCode 班级代码
     * @param userId 学生ID
     * @return
     */
    boolean joinClass(String classCode, Long userId);

    /**
     * 学生退出班级
     * @param classId 班级ID
     * @param userId 学生ID
     * @return
     */
    boolean leaveClass(Long classId, Long userId);

    /**
     * 获取学生加入的班级列表
     * @param userId 学生ID
     * @return
     */
    java.util.List<ClassVO> getStudentClasses(Long userId);

    boolean removeById(Long id);
}
