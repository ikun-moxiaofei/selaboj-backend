package com.mxf.selaboj.judge.codesandbox.impl;

import com.mxf.selaboj.judge.codesandbox.CodeSandbox;
import com.mxf.selaboj.judge.codesandbox.CodeSandboxFactory;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class CodeSandboxTest {

    @Value("${codesandbox.type}")
    private String type;

    @Test
    void executeCode() {
        System.out.println(type);
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "int main() { }";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExcuteCodeRequest executeCodeRequest = ExcuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExcuteCodeResponse executeCodeResponse = codeSandbox.excuteCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }
}