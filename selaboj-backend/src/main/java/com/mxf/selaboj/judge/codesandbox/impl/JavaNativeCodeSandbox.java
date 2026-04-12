package com.mxf.selaboj.judge.codesandbox.impl;

import com.mxf.selaboj.judge.codesandbox.CodeSandbox;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;
import com.mxf.selaboj.model.dto.questionsubmit.JudgeInfo;
import com.mxf.selaboj.model.enums.JudgeInfoMessageEnum;
import com.mxf.selaboj.model.enums.QuestionSubmitStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Java原生代码沙箱（实际执行Java代码）
 */
@Slf4j
public class JavaNativeCodeSandbox implements CodeSandbox {
    private static final String PREFIX = "Main";
    private static final String JAVA_FILE_SUFFIX = ".java";
    private static final String CLASS_FILE_SUFFIX = ".class";

    @Override
    public ExcuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest) {
        List<String> inputList = excuteCodeRequest.getInputList();
        String code = excuteCodeRequest.getCode();
        String language = excuteCodeRequest.getLanguage();

        // 1. 创建临时目录
        String userDir = System.getProperty("user.dir");
        String tempDir = userDir + File.separator + "temp" + File.separator + UUID.randomUUID();
        File tempDirectory = new File(tempDir);
        if (!tempDirectory.exists()) {
            boolean mkdirs = tempDirectory.mkdirs();
            if (!mkdirs) {
                return getErrorResponse("创建临时目录失败");
            }
        }

        // 2. 写入Java文件
        String fileName = PREFIX + JAVA_FILE_SUFFIX;
        File javaFile = new File(tempDirectory, fileName);
        try (FileWriter fileWriter = new FileWriter(javaFile)) {
            fileWriter.write(code);
            fileWriter.flush();
        } catch (IOException e) {
            log.error("写入Java文件失败", e);
            return getErrorResponse("写入文件失败");
        }

        // 3. 编译Java文件
        String compileCommand = String.format("javac -d %s %s", tempDir, javaFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCommand);
            int compileExitCode = compileProcess.waitFor();
            if (compileExitCode != 0) {
                // 编译失败
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(compileProcess.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorMessage = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorMessage.append(line).append("\n");
                    }
                    return getErrorResponse("编译失败: " + errorMessage.toString());
                }
            }
        } catch (Exception e) {
            log.error("编译Java文件失败", e);
            return getErrorResponse("编译失败");
        }

        // 4. 执行Java程序
        List<String> outputList = new ArrayList<>();
        long totalTime = 0;
        long maxMemory = 0;

        for (String input : inputList) {
            String runCommand = String.format("java -cp %s Main", tempDir);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCommand);

                // 写入输入
                try (OutputStream outputStream = runProcess.getOutputStream()) {
                    outputStream.write((input + "\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }

                // 记录开始时间
                long startTime = System.currentTimeMillis();
                long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                // 等待执行完成
                int runExitCode = runProcess.waitFor();

                // 记录结束时间和内存
                long endTime = System.currentTimeMillis();
                long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                totalTime += (endTime - startTime);
                maxMemory = Math.max(maxMemory, endMemory - startMemory);

                if (runExitCode != 0) {
                    // 运行失败
                    try (BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(runProcess.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder errorMessage = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorMessage.append(line).append("\n");
                        }
                        return getErrorResponse("运行失败: " + errorMessage.toString());
                    }
                }

                // 读取输出
                try (BufferedReader outputReader = new BufferedReader(
                        new InputStreamReader(runProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = outputReader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    outputList.add(output.toString().trim());
                }

            } catch (Exception e) {
                log.error("执行Java程序失败", e);
                return getErrorResponse("执行失败");
            }
        }

        // 5. 清理临时文件
        cleanTempFiles(tempDirectory);

        // 6. 构建响应
        ExcuteCodeResponse response = new ExcuteCodeResponse();
        response.setOutputList(outputList);
        response.setMessage("执行成功");
        response.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(maxMemory / 1024); // 转换为KB
        judgeInfo.setTime(totalTime / inputList.size()); // 平均时间
        response.setJudgeInfo(judgeInfo);

        return response;
    }

    /**
     * 获取错误响应
     */
    private ExcuteCodeResponse getErrorResponse(String message) {
        ExcuteCodeResponse response = new ExcuteCodeResponse();
        response.setOutputList(new ArrayList<>());
        response.setMessage(message);
        response.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getText());
        judgeInfo.setMemory(0L);
        judgeInfo.setTime(0L);
        response.setJudgeInfo(judgeInfo);
        return response;
    }

    /**
     * 清理临时文件
     */
    private void cleanTempFiles(File tempDirectory) {
        if (tempDirectory.exists()) {
            File[] files = tempDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            tempDirectory.delete();
        }
    }
}
