package com.mxf.selaboj.judge.codesandbox.impl;

import com.mxf.selaboj.judge.codesandbox.CodeSandbox;
import com.mxf.selaboj.judge.codesandbox.CodeSandboxFactory;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;
import com.mxf.selaboj.model.enums.QuestionSubmitLanguageEnum;

import java.util.Arrays;
import java.util.List;

public class JavaNativeCodeSandboxTest {
    public static void main(String[] args) {
        System.out.println("Starting JavaNativeCodeSandbox test...");
        
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance("javaNative");
        
        String code = "import java.util.Scanner;\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner scanner = new Scanner(System.in);\n" +
                "        int a = scanner.nextInt();\n" +
                "        int b = scanner.nextInt();\n" +
                "        System.out.println(a + b);\n" +
                "    }\n" +
                "}";
        
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("11 2", "3 4", "10 20", "101 20", "102 20", "130 20");
        
        ExcuteCodeRequest executeCodeRequest = ExcuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        
        try {
            ExcuteCodeResponse executeCodeResponse = codeSandbox.excuteCode(executeCodeRequest);
            
            System.out.println("Status: " + executeCodeResponse.getStatus());
            System.out.println("Message: " + executeCodeResponse.getMessage());
            System.out.println("Output: " + executeCodeResponse.getOutputList());
            System.out.println("Judge Info: " + executeCodeResponse.getJudgeInfo());
            
            if (executeCodeResponse.getStatus() == 2) {
                System.out.println("SUCCESS: Code sandbox test passed!");
            } else {
                System.out.println("FAILED: Code sandbox test failed!");
            }
        } catch (Exception e) {
            System.err.println("Exception during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
