package com.mxf.selaboj.judge.codesandbox.impl;

import com.mxf.selaboj.judge.codesandbox.CodeSandbox;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;
import com.mxf.selaboj.model.dto.questionsubmit.JudgeInfo;
import com.mxf.selaboj.model.enums.JudgeInfoMessageEnum;
import com.mxf.selaboj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

public class ThirdPartyCodeSandbox implements CodeSandbox {

    @Override
    public ExcuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest) {
        List<String> inputList = excuteCodeRequest.getInputList();
        String code = excuteCodeRequest.getCode();
        String language = excuteCodeRequest.getLanguage();

        System.out.println("第三方代码沙箱");

        ExcuteCodeResponse excuteCodeResponse = new ExcuteCodeResponse();
        excuteCodeResponse.setMessage("第三方代码沙箱 successful");
        excuteCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setTime(100L);
        judgeInfo.setMemory(100L);
        excuteCodeResponse.setJudgeInfo(judgeInfo);

        return excuteCodeResponse;
    }
}
