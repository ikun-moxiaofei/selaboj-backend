package com.mxf.selaboj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mxf.selaboj.common.BaseResponse;
import com.mxf.selaboj.common.DeleteRequest;
import com.mxf.selaboj.common.ErrorCode;
import com.mxf.selaboj.common.ResultUtils;
import com.mxf.selaboj.exception.BusinessException;
import com.mxf.selaboj.exception.ThrowUtils;
import com.mxf.selaboj.model.dto.classs.ClassAddRequest;
import com.mxf.selaboj.model.dto.classs.ClassEditRequest;
import com.mxf.selaboj.model.dto.classs.ClassMemberRequest;
import com.mxf.selaboj.model.dto.classs.ClassQueryRequest;
import com.mxf.selaboj.model.entity.Class;
import com.mxf.selaboj.model.vo.ClassVO;
import com.mxf.selaboj.model.vo.UserVO;
import com.mxf.selaboj.model.entity.User;
import com.mxf.selaboj.service.ClassService;
import com.mxf.selaboj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 班级控制器
 */
@RestController
@RequestMapping("/class")
@Slf4j
public class ClassController {

    @Resource
    private ClassService classService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建班级
     *
     * @param classAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addClass(@RequestBody ClassAddRequest classAddRequest, HttpServletRequest request) {
        if (classAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查是否为老师或管理员
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "只有老师可以创建班级");
        }
        Class clazz = new Class();
        clazz.setClassName(classAddRequest.getClassName());
        clazz.setClassCode(classAddRequest.getClassCode());
        clazz.setDescription(classAddRequest.getDescription());
        classService.validClass(clazz, true);
        boolean result = classService.save(clazz);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newClassId = clazz.getId();
        return ResultUtils.success(newClassId);
    }

    /**
     * 删除班级
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteClass(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        Class oldClass = classService.getById(id);
        ThrowUtils.throwIf(oldClass == null, ErrorCode.NOT_FOUND_ERROR);
        boolean b = classService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新班级
     *
     * @param classEditRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateClass(@RequestBody ClassEditRequest classEditRequest) {
        if (classEditRequest == null || classEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Class clazz = new Class();
        clazz.setId(classEditRequest.getId());
        clazz.setClassName(classEditRequest.getClassName());
        clazz.setClassCode(classEditRequest.getClassCode());
        clazz.setDescription(classEditRequest.getDescription());
        // 参数校验
        classService.validClass(clazz, false);
        long id = classEditRequest.getId();
        // 判断是否存在
        Class oldClass = classService.getById(id);
        ThrowUtils.throwIf(oldClass == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = classService.updateById(clazz);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取班级
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Class> getClassById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Class clazz = classService.getById(id);
        if (clazz == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(clazz);
    }

    /**
     * 根据 id 获取班级（脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ClassVO> getClassVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Class clazz = classService.getById(id);
        if (clazz == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(classService.getClassVO(clazz, request));
    }

    /**
     * 分页获取班级列表（封装类）
     *
     * @param classQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ClassVO>> listClassVOByPage(@RequestBody ClassQueryRequest classQueryRequest,
            HttpServletRequest request) {
        long current = classQueryRequest.getCurrent();
        long size = classQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Class> classPage = classService.page(new Page<>(current, size),
                classService.getQueryWrapper(classQueryRequest));
        return ResultUtils.success(classService.getClassVOPage(classPage, request));
    }

    // endregion

    /**
     * 添加班级成员
     *
     * @param classMemberRequest
     * @param request
     * @return
     */
    @PostMapping("/member/add")
    public BaseResponse<Boolean> addClassMembers(@RequestBody ClassMemberRequest classMemberRequest,
            HttpServletRequest request) {
        if (classMemberRequest == null || classMemberRequest.getClassId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = classService.addClassMembers(classMemberRequest.getClassId(), classMemberRequest.getUserIds());
        return ResultUtils.success(result);
    }

    /**
     * 移除班级成员
     *
     * @param classMemberRequest
     * @param request
     * @return
     */
    @PostMapping("/member/remove")
    public BaseResponse<Boolean> removeClassMembers(@RequestBody ClassMemberRequest classMemberRequest,
            HttpServletRequest request) {
        if (classMemberRequest == null || classMemberRequest.getClassId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = classService.removeClassMembers(classMemberRequest.getClassId(),
                classMemberRequest.getUserIds());
        return ResultUtils.success(result);
    }

    /**
     * 获取班级成员列表
     *
     * @param classId
     * @param request
     * @return
     */
    @GetMapping("/member/list")
    public BaseResponse<List<UserVO>> getClassMembers(long classId, HttpServletRequest request) {
        if (classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<UserVO> userVOList = classService.getClassMembers(classId);
        return ResultUtils.success(userVOList);
    }

    /**
     * 学生加入班级
     *
     * @param classCode 班级代码
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinClass(@RequestParam String classCode, HttpServletRequest request) {
        if (classCode == null || classCode.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "班级代码不能为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = classService.joinClass(classCode, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 学生退出班级
     *
     * @param classId 班级ID
     * @param request
     * @return
     */
    @PostMapping("/leave")
    public BaseResponse<Boolean> leaveClass(@RequestParam Long classId, HttpServletRequest request) {
        if (classId == null || classId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = classService.leaveClass(classId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 获取学生加入的班级列表
     *
     * @param request
     * @return
     */
    @GetMapping("/my/list")
    public BaseResponse<List<ClassVO>> getMyClasses(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<ClassVO> classVOList = classService.getStudentClasses(loginUser.getId());
        return ResultUtils.success(classVOList);
    }
}
