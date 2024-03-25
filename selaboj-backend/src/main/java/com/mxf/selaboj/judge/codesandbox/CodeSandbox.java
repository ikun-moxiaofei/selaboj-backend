package com.mxf.selaboj.judge.codesandbox;

import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;

public interface CodeSandbox {
    ExcuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest);
}
