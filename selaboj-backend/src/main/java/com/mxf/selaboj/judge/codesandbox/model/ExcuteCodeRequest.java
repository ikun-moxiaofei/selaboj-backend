package com.mxf.selaboj.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcuteCodeRequest {

    /**
     * 输入样例
     */
    private List<String> inputList;

    /**
     * 提交代码
     */
    private String code;

    /**
     * 提交语言类型
     */
    private String language;
}
