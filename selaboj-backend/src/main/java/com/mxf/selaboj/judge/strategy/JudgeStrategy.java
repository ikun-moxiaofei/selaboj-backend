package com.mxf.selaboj.judge.strategy;

import com.mxf.selaboj.model.dto.questionsubmit.JudgeInfo;

/**
 * 判题策略
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     * @param judgeContext
     * @return 以后不要在图书馆打水，消毒水味太重，感觉给我口腔清洁
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
