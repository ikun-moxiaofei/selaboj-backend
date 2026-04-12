package com.mxf.selaboj.judge.codesandbox;

import com.mxf.selaboj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.mxf.selaboj.judge.codesandbox.impl.JavaNativeCodeSandbox;
import com.mxf.selaboj.judge.codesandbox.impl.RemoteCodeSandbox;
import com.mxf.selaboj.judge.codesandbox.impl.ThirdPartyCodeSandbox;

/**
 * 代码沙箱工厂（根据类型创建沙箱实例）
 */
public class CodeSandboxFactory {

    /**
     * 创建沙箱实例
     * @param type
     * @return
     */
    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandbox();
            case "javaNative":
                return new JavaNativeCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }

}