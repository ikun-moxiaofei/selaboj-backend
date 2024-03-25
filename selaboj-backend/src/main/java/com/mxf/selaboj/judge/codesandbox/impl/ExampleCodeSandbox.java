package com.mxf.selaboj.judge.codesandbox.impl;

import com.mxf.selaboj.judge.codesandbox.CodeSandbox;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;
import com.mxf.selaboj.model.dto.questionsubmit.JudgeInfo;
import com.mxf.selaboj.model.enums.JudgeInfoMessageEnum;
import com.mxf.selaboj.model.enums.QuestionSubmitStatusEnum;
import lombok.extern.slf4j.Slf4j;


import java.util.List;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 */
@Slf4j
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExcuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest) {
        List<String> inputList = excuteCodeRequest.getInputList();
        ExcuteCodeResponse executeCodeResponse = new ExcuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}