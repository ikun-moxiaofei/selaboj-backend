package com.mxf.selaboj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxf.selaboj.common.ErrorCode;
import com.mxf.selaboj.constant.CommonConstant;
import com.mxf.selaboj.exception.BusinessException;
import com.mxf.selaboj.exception.ThrowUtils;
import com.mxf.selaboj.mapper.ClassMapper;
import com.mxf.selaboj.mapper.ClassStudentMapper;
import com.mxf.selaboj.model.dto.classs.ClassQueryRequest;
import com.mxf.selaboj.model.entity.Class;
import com.mxf.selaboj.model.entity.ClassStudent;
import com.mxf.selaboj.model.vo.ClassVO;
import com.mxf.selaboj.model.vo.UserVO;
import com.mxf.selaboj.service.ClassService;
import com.mxf.selaboj.service.UserService;
import com.mxf.selaboj.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.CollectionUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级服务实现
 */
@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class>
        implements ClassService {

    @Resource
    private UserService userService;

    @Resource
    private ClassStudentMapper classStudentMapper;

    /**
     * 校验班级是否合法
     * @param clazz
     * @param add
     */
    @Override
    public void validClass(Class clazz, boolean add) {
        if (clazz == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String className = clazz.getClassName();
        String classCode = clazz.getClassCode();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(className, classCode), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(className) && className.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "班级名称过长");
        }
        if (StringUtils.isNotBlank(classCode) && classCode.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "班级代码过长");
        }
        // 检查班级代码是否重复
        if (add || StringUtils.isNotBlank(classCode)) {
            QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("classCode", classCode);
            if (!add) {
                queryWrapper.ne("id", clazz.getId());
            }
            if (this.count(queryWrapper) > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "班级代码已存在");
            }
        }
    }

    /**
     * 获取查询条件
     *
     * @param classQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Class> getQueryWrapper(ClassQueryRequest classQueryRequest) {
        QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
        if (classQueryRequest == null) {
            return queryWrapper;
        }
        Long id = classQueryRequest.getId();
        String className = classQueryRequest.getClassName();
        String classCode = classQueryRequest.getClassCode();
        String sortField = classQueryRequest.getSortField();
        String sortOrder = classQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(className), "className", className);
        queryWrapper.like(StringUtils.isNotBlank(classCode), "classCode", classCode);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public ClassVO getClassVO(Class clazz, HttpServletRequest request) {
        ClassVO classVO = ClassVO.objToVo(clazz);
        return classVO;
    }

    @Override
    public Page<ClassVO> getClassVOPage(Page<Class> classPage, HttpServletRequest request) {
        List<Class> classList = classPage.getRecords();
        Page<ClassVO> classVOPage = new Page<>(classPage.getCurrent(), classPage.getSize(), classPage.getTotal());
        if (CollectionUtils.isEmpty(classList)) {
            return classVOPage;
        }
        // 填充信息
        List<ClassVO> classVOList = classList.stream().map(clazz -> {
            ClassVO classVO = ClassVO.objToVo(clazz);
            return classVO;
        }).collect(Collectors.toList());
        classVOPage.setRecords(classVOList);
        return classVOPage;
    }

    @Override
    public boolean addClassMembers(Long classId, List<Long> userIds) {
        if (classId == null || CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查班级是否存在
        Class clazz = this.getById(classId);
        ThrowUtils.throwIf(clazz == null, ErrorCode.NOT_FOUND_ERROR, "班级不存在");
        // 检查用户是否存在
        List<com.mxf.selaboj.model.entity.User> users = userService.listByIds(userIds);
        ThrowUtils.throwIf(users.size() != userIds.size(), ErrorCode.PARAMS_ERROR, "部分用户不存在");
        // 批量添加班级成员
        List<ClassStudent> classStudents = new ArrayList<>();
        for (Long userId : userIds) {
            // 检查是否已经是班级成员
            QueryWrapper<ClassStudent> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("classId", classId);
            queryWrapper.eq("userId", userId);
            queryWrapper.eq("isDelete", false);
            if (classStudentMapper.selectCount(queryWrapper) == 0) {
                ClassStudent classStudent = new ClassStudent();
                classStudent.setClassId(classId);
                classStudent.setUserId(userId);
                classStudents.add(classStudent);
            }
        }
        if (CollectionUtils.isNotEmpty(classStudents)) {
            for (ClassStudent classStudent : classStudents) {
                classStudentMapper.insert(classStudent);
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean removeClassMembers(Long classId, List<Long> userIds) {
        if (classId == null || CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查班级是否存在
        Class clazz = this.getById(classId);
        ThrowUtils.throwIf(clazz == null, ErrorCode.NOT_FOUND_ERROR, "班级不存在");
        // 批量移除班级成员
        QueryWrapper<ClassStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("classId", classId);
        queryWrapper.in("userId", userIds);
        queryWrapper.eq("isDelete", false);
        return classStudentMapper.delete(queryWrapper) > 0;
    }

    @Override
    public List<UserVO> getClassMembers(Long classId) {
        if (classId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查班级是否存在
        Class clazz = this.getById(classId);
        ThrowUtils.throwIf(clazz == null, ErrorCode.NOT_FOUND_ERROR, "班级不存在");
        // 获取班级成员
        QueryWrapper<ClassStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("classId", classId);
        queryWrapper.eq("isDelete", false);
        List<ClassStudent> classStudents = classStudentMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(classStudents)) {
            return new ArrayList<>();
        }
        // 获取用户信息
        List<Long> userIds = classStudents.stream().map(ClassStudent::getUserId).collect(Collectors.toList());
        List<com.mxf.selaboj.model.entity.User> users = userService.listByIds(userIds);
        // 转换为VO
        return users.stream().map(userService::getUserVO).collect(Collectors.toList());
    }

    @Override
    public boolean joinClass(String classCode, Long userId) {
        if (StringUtils.isBlank(classCode) || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据班级代码查找班级
        QueryWrapper<Class> classQueryWrapper = new QueryWrapper<>();
        classQueryWrapper.eq("classCode", classCode);
        classQueryWrapper.eq("isDelete", false);
        Class clazz = this.getOne(classQueryWrapper);
        ThrowUtils.throwIf(clazz == null, ErrorCode.NOT_FOUND_ERROR, "班级不存在或班级代码错误");
        
        // 检查是否已经是班级成员
        QueryWrapper<ClassStudent> studentQueryWrapper = new QueryWrapper<>();
        studentQueryWrapper.eq("classId", clazz.getId());
        studentQueryWrapper.eq("userId", userId);
        studentQueryWrapper.eq("isDelete", false);
        if (classStudentMapper.selectCount(studentQueryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已经加入该班级");
        }
        
        // 添加学生到班级
        ClassStudent classStudent = new ClassStudent();
        classStudent.setClassId(clazz.getId());
        classStudent.setUserId(userId);
        return classStudentMapper.insert(classStudent) > 0;
    }

    @Override
    public boolean leaveClass(Long classId, Long userId) {
        if (classId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查班级是否存在
        Class clazz = this.getById(classId);
        ThrowUtils.throwIf(clazz == null, ErrorCode.NOT_FOUND_ERROR, "班级不存在");
        
        // 移除学生
        QueryWrapper<ClassStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("classId", classId);
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("isDelete", false);
        return classStudentMapper.delete(queryWrapper) > 0;
    }

    @Override
    public List<ClassVO> getStudentClasses(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取学生加入的班级ID列表
        QueryWrapper<ClassStudent> studentQueryWrapper = new QueryWrapper<>();
        studentQueryWrapper.eq("userId", userId);
        studentQueryWrapper.eq("isDelete", false);
        List<ClassStudent> classStudents = classStudentMapper.selectList(studentQueryWrapper);
        
        if (CollectionUtils.isEmpty(classStudents)) {
            return new ArrayList<>();
        }
        
        // 获取班级信息
        List<Long> classIds = classStudents.stream().map(ClassStudent::getClassId).collect(Collectors.toList());
        List<Class> classes = this.listByIds(classIds);
        
        // 转换为VO
        return classes.stream().map(clazz -> {
            ClassVO classVO = new ClassVO();
            classVO.setId(clazz.getId());
            classVO.setClassName(clazz.getClassName());
            classVO.setClassCode(clazz.getClassCode());
            classVO.setDescription(clazz.getDescription());
            classVO.setCreateTime(clazz.getCreateTime());
            return classVO;
        }).collect(Collectors.toList());
    }
}
