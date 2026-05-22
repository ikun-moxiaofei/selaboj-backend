package com.mxf.selaboj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mxf.selaboj.model.entity.ExamRecord;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 考试记录视图
 */
@Data
public class ExamRecordVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 考试 id
     */
    private Long examId;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 状态（0 - 待考试，1 - 考试中，2 - 已完成）
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 答题记录
     */
    private List<ExamAnswerVO> answers;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 包装类转对象
     *
     * @param examRecordVO
     * @return
     */
    public static ExamRecord voToObj(ExamRecordVO examRecordVO) {
        if (examRecordVO == null) {
            return null;
        }
        ExamRecord examRecord = new ExamRecord();
        BeanUtils.copyProperties(examRecordVO, examRecord);
        return examRecord;
    }

    /**
     * 对象转包装类
     *
     * @param examRecord
     * @return
     */
    public static ExamRecordVO objToVo(ExamRecord examRecord) {
        if (examRecord == null) {
            return null;
        }
        ExamRecordVO examRecordVO = new ExamRecordVO();
        BeanUtils.copyProperties(examRecord, examRecordVO);
        // 状态文本
        switch (examRecord.getStatus()) {
            case 0:
                examRecordVO.setStatusText("待考试");
                break;
            case 1:
                examRecordVO.setStatusText("考试中");
                break;
            case 2:
                examRecordVO.setStatusText("已完成");
                break;
            default:
                examRecordVO.setStatusText("未知");
        }
        return examRecordVO;
    }

    private static final long serialVersionUID = 1L;
}
