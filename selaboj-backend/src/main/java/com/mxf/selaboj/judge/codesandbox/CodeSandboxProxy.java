package com.mxf.selaboj.judge.codesandbox;

import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeSandboxProxy implements CodeSandbox {

    private final CodeSandbox codeSandbox;


    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }

    @Override
    public ExcuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest) {
        log.info("代码沙箱请求信息：" + excuteCodeRequest.toString());
        ExcuteCodeResponse excuteCodeResponse = codeSandbox.excuteCode(excuteCodeRequest);
        log.info("代码沙箱响应信息：" + excuteCodeResponse.toString());
        return excuteCodeResponse;
    }   
}