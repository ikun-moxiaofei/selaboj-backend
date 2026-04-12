package com.mxf.selaboj.controller;

import com.mxf.selaboj.common.BaseResponse;
import com.mxf.selaboj.common.ErrorCode;
import com.mxf.selaboj.common.ResultUtils;
import com.mxf.selaboj.exception.BusinessException;
import com.mxf.selaboj.judge.codesandbox.CodeSandbox;
import com.mxf.selaboj.judge.codesandbox.CodeSandboxFactory;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/code")
@Slf4j
public class CodeController {

    @PostMapping("/run")
    public BaseResponse<ExcuteCodeResponse> runCode(@RequestBody CodeRunRequest codeRunRequest) {
        if (codeRunRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        String code = codeRunRequest.getCode();
        String language = codeRunRequest.getLanguage();
        List<String> inputList = codeRunRequest.getInputList();
        
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码不能为空");
        }
        
        if (inputList == null || inputList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入用例不能为空");
        }
        
        // 创建代码沙箱实例
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance("javaNative");
        
        // 构建执行请求
        ExcuteCodeRequest excuteCodeRequest = ExcuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        
        // 执行代码
        ExcuteCodeResponse response = codeSandbox.excuteCode(excuteCodeRequest);
        
        return ResultUtils.success(response);
    }
    
    /**
     * 代码运行请求
     */
    public static class CodeRunRequest {
        private String code;
        private String language;
        private List<String> inputList;
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getLanguage() {
            return language;
        }
        
        public void setLanguage(String language) {
            this.language = language;
        }
        
        public List<String> getInputList() {
            return inputList;
        }
        
        public void setInputList(List<String> inputList) {
            this.inputList = inputList;
        }
    }
}
