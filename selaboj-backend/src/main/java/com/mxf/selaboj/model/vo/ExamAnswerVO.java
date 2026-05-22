package com.mxf.selaboj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mxf.selaboj.model.entity.ExamAnswer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 考试答题记录视图
 */
@Data
public class ExamAnswerVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 考试记录 id
     */
    private Long examRecordId;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 题目名称
     */
    private String questionTitle;

    /**
     * 题目类型
     */
    private Integer questionType;

    /**
     * 题目分值
     */
    private Integer questionScore;

    /**
     * 答案
     */
    private String answer;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 状态（0 - 未作答，1 - 已作答，2 - 正确，3 - 错误）
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 包装类转对象
     *
     * @param examAnswerVO
     * @return
     */
    public static ExamAnswer voToObj(ExamAnswerVO examAnswerVO) {
        if (examAnswerVO == null) {
            return null;
        }
        ExamAnswer examAnswer = new ExamAnswer();
        BeanUtils.copyProperties(examAnswerVO, examAnswer);
        return examAnswer;
    }

    /**
     * 对象转包装类
     *
     * @param examAnswer
     * @return
     */
    public static ExamAnswerVO objToVo(ExamAnswer examAnswer) {
        if (examAnswer == null) {
            return null;
        }
        ExamAnswerVO examAnswerVO = new ExamAnswerVO();
        BeanUtils.copyProperties(examAnswer, examAnswerVO);
        // 状态文本
        switch (examAnswer.getStatus()) {
            case 0:
                examAnswerVO.setStatusText("未作答");
                break;
            case 1:
                examAnswerVO.setStatusText("已作答");
                break;
            case 2:
                examAnswerVO.setStatusText("正确");
                break;
            case 3:
                examAnswerVO.setStatusText("错误");
                break;
            default:
                examAnswerVO.setStatusText("未知");
        }
        return examAnswerVO;
    }

    private static final long serialVersionUID = 1L;
}
