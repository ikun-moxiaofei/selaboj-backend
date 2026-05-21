package com.mxf.selaboj.controller;

import com.mxf.selaboj.common.BaseResponse;
import com.mxf.selaboj.common.ResultUtils;
import com.mxf.selaboj.service.ExamService;
import com.mxf.selaboj.service.QuestionService;
import com.mxf.selaboj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@Slf4j
public class StatisticsController {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    private ExamService examService;

    @GetMapping("/get")
    public BaseResponse<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long userCount = userService.count();
        long questionCount = questionService.count();
        long examCount = examService.count();

        stats.put("userCount", userCount);
        stats.put("questionCount", questionCount);
        stats.put("examCount", examCount);
        stats.put("systemStability", "99.9%");

        return ResultUtils.success(stats);
    }
}