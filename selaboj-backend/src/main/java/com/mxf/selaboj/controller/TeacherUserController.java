package com.mxf.selaboj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mxf.selaboj.common.BaseResponse;
import com.mxf.selaboj.common.ErrorCode;
import com.mxf.selaboj.common.ResultUtils;
import com.mxf.selaboj.exception.BusinessException;
import com.mxf.selaboj.mapper.UserMapper;
import com.mxf.selaboj.model.entity.User;
import com.mxf.selaboj.model.vo.UserVO;
import com.mxf.selaboj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/teacher/user")
@Slf4j
public class TeacherUserController {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    /**
     * 获取学生列表（老师视角，只能看到学生）
     */
    @GetMapping("/list")
    public BaseResponse<Page<UserVO>> listStudents(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userAccount,
            HttpServletRequest request) {
        
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以查看学生列表");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userRole", "student");
        queryWrapper.eq("isDelete", false);
        
        if (userName != null && !userName.isEmpty()) {
            queryWrapper.like("userName", userName);
        }
        if (userAccount != null && !userAccount.isEmpty()) {
            queryWrapper.like("userAccount", userAccount);
        }
        
        queryWrapper.orderByDesc("createTime");

        Page<User> userPage = userMapper.selectPage(new Page<>(current, pageSize), queryWrapper);
        Page<UserVO> voPage = new Page<>(current, pageSize, userPage.getTotal());
        voPage.setRecords(userService.getUserVO(userPage.getRecords()));

        return ResultUtils.success(voPage);
    }

    /**
     * 获取单个学生信息
     */
    @GetMapping("/get/{id}")
    public BaseResponse<UserVO> getStudent(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以查看学生信息");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        if (!"student".equals(user.getUserRole()) && !loginUser.getUserRole().equals("admin")) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能查看学生信息");
        }

        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 修改学生信息（老师只能修改学生的基本信息，不能改角色）
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateStudent(@RequestBody User user, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以修改学生信息");
        }

        if (user.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }

        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        if (!"student".equals(existingUser.getUserRole()) && !loginUser.getUserRole().equals("admin")) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能修改学生信息");
        }

        user.setUserRole(existingUser.getUserRole());
        user.setUserPassword(existingUser.getUserPassword());

        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     * 删除学生（老师只能删除学生）
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteStudent(@RequestParam Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以删除学生");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        if (!"student".equals(user.getUserRole()) && !loginUser.getUserRole().equals("admin")) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能删除学生");
        }

        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 创建学生账号（老师可以创建学生）
     */
    @PostMapping("/add")
    public BaseResponse<Long> addStudent(@RequestBody User user, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"teacher".equals(loginUser.getUserRole()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有老师或管理员可以创建学生账号");
        }

        if (user.getUserAccount() == null || user.getUserAccount().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能为空");
        }

        user.setUserRole("student");
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex(("mxf" + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);

        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建失败");
        }

        return ResultUtils.success(user.getId());
    }
}