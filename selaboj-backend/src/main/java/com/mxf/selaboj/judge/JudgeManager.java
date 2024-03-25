package com.mxf.selaboj.judge;

import com.mxf.selaboj.judge.strategy.DefaultJudgeStrategy;
import com.mxf.selaboj.judge.strategy.JavaLanguageJudgeStrategy;
import com.mxf.selaboj.judge.strategy.JudgeContext;
import com.mxf.selaboj.judge.strategy.JudgeStrategy;
import com.mxf.selaboj.model.dto.questionsubmit.JudgeInfo;
import com.mxf.selaboj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}