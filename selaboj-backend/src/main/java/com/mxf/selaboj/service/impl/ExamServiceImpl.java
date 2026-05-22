package com.mxf.selaboj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mxf.selaboj.common.ErrorCode;
import com.mxf.selaboj.constant.CommonConstant;
import com.mxf.selaboj.exception.BusinessException;
import com.mxf.selaboj.exception.ThrowUtils;
import com.mxf.selaboj.mapper.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mxf.selaboj.model.dto.exam.ExamAddRequest;
import com.mxf.selaboj.model.dto.exam.ExamEditRequest;
import com.mxf.selaboj.model.dto.exam.ExamQuestionRequest;
import com.mxf.selaboj.model.dto.exam.ExamQueryRequest;
import com.mxf.selaboj.model.dto.question.JudgeCase;
import com.mxf.selaboj.model.dto.question.QuestionOption;
import com.mxf.selaboj.model.entity.*;
import com.mxf.selaboj.model.vo.*;
import com.mxf.selaboj.service.ExamService;
import com.mxf.selaboj.service.UserService;
import com.mxf.selaboj.utils.SqlUtils;
import com.mxf.selaboj.judge.codesandbox.CodeSandbox;
import com.mxf.selaboj.judge.codesandbox.CodeSandboxFactory;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeRequest;
import com.mxf.selaboj.judge.codesandbox.model.ExcuteCodeResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试服务实现
 */
@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam>
        implements ExamService {

    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private ExamQuestionMapper examQuestionMapper;

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private ExamAnswerMapper examAnswerMapper;

    @Resource
    private ClassMapper classMapper;

    @Resource
    private ClassStudentMapper classStudentMapper;

    /**
     * 校验考试是否合法
     * 
     * @param exam
     * @param add
     */
    @Override
    public void validExam(Exam exam, boolean add) {
        if (exam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String examName = exam.getExamName();
        Date startTime = exam.getStartTime();
        Date endTime = exam.getEndTime();
        Integer totalScore = exam.getTotalScore();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(examName), ErrorCode.PARAMS_ERROR, "考试名称不能为空");
            ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "考试时间不能为空");
            ThrowUtils.throwIf(totalScore == null || totalScore <= 0, ErrorCode.PARAMS_ERROR, "总分必须大于0");
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(examName) && examName.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "考试名称过长");
        }
        if (startTime != null && endTime != null && startTime.after(endTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "开始时间不能晚于结束时间");
        }
        if (totalScore != null && totalScore <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "总分必须大于0");
        }
    }

    /**
     * 获取查询条件
     *
     * @param examQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Exam> getQueryWrapper(ExamQueryRequest examQueryRequest) {
        QueryWrapper<Exam> queryWrapper = new QueryWrapper<>();
        if (examQueryRequest == null) {
            return queryWrapper;
        }
        Long id = examQueryRequest.getId();
        String examName = examQueryRequest.getExamName();
        String keyword = examQueryRequest.getKeyword();
        Date startTime = examQueryRequest.getStartTime();
        Date endTime = examQueryRequest.getEndTime();
        String sortField = examQueryRequest.getSortField();
        String sortOrder = examQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(examName), "examName", examName);
        queryWrapper.like(StringUtils.isNotBlank(keyword), "examName", keyword);
        queryWrapper.ge(startTime != null, "startTime", startTime);
        queryWrapper.le(endTime != null, "endTime", endTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                StringUtils.equals(sortOrder, CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public ExamVO getExamVO(Exam exam, HttpServletRequest request) {
        ExamVO examVO = ExamVO.objToVo(exam);
        // 获取题目信息
        QueryWrapper<ExamQuestion> examQuestionQueryWrapper = new QueryWrapper<>();
        examQuestionQueryWrapper.eq("examId", exam.getId());
        examQuestionQueryWrapper.eq("isDelete", false);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionQueryWrapper);

        if (CollectionUtils.isNotEmpty(examQuestions)) {
            examVO.setQuestionCount(examQuestions.size());
            List<ExamQuestionVO> questionVOs = new ArrayList<>();
            for (ExamQuestion examQuestion : examQuestions) {
                Question question = questionMapper.selectById(examQuestion.getQuestionId());
                if (question != null) {
                    ExamQuestionVO examQuestionVO = new ExamQuestionVO();
                    examQuestionVO.setQuestionId(question.getId());
                    examQuestionVO.setQuestionTitle(question.getTitle());
                    examQuestionVO.setQuestionType(question.getQuestionType());
                    examQuestionVO.setScore(examQuestion.getScore());
                    examQuestionVO.setQuestion(QuestionVO.objToVo(question));
                    // 为选择题添加选项信息
                    if (question.getQuestionType() == 1) {
                        log.info("选择题ID: {}, 选项: {}", question.getId(), question.getOptions());
                        if (StringUtils.isNotBlank(question.getOptions())) {
                            try {
                                List<QuestionOption> options = GSON.fromJson(question.getOptions(),
                                        new TypeToken<List<QuestionOption>>() {
                                        }.getType());
                                examQuestionVO.setOptions(options);
                                log.info("解析选项成功: {}", options);
                            } catch (Exception e) {
                                log.error("解析选项失败", e);
                            }
                        } else {
                            log.warn("选择题无选项: {}", question.getId());
                        }
                    }
                    questionVOs.add(examQuestionVO);
                }
            }
            examVO.setQuestions(questionVOs);
        }
        return examVO;
    }

    @Override
    public Page<ExamVO> getExamVOPage(Page<Exam> examPage, HttpServletRequest request) {
        List<Exam> examList = examPage.getRecords();
        Page<ExamVO> examVOPage = new Page<>(examPage.getCurrent(), examPage.getSize(), examPage.getTotal());
        if (CollectionUtils.isEmpty(examList)) {
            return examVOPage;
        }
        // 填充信息
        List<ExamVO> examVOList = examList.stream().map(exam -> {
            ExamVO examVO = getExamVO(exam, request);
            return examVO;
        }).collect(Collectors.toList());
        examVOPage.setRecords(examVOList);
        return examVOPage;
    }

    @Override
    public long addExam(ExamAddRequest examAddRequest, HttpServletRequest request) {
        Exam exam = new Exam();
        exam.setExamName(examAddRequest.getExamName());
        exam.setDescription(examAddRequest.getDescription());
        exam.setStartTime(examAddRequest.getStartTime());
        exam.setEndTime(examAddRequest.getEndTime());
        exam.setTotalScore(examAddRequest.getTotalScore());
        validExam(exam, true);

        // 保存考试
        boolean saved = this.save(exam);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "考试创建失败");

        // 保存题目关联
        List<ExamQuestionRequest> questions = examAddRequest.getQuestions();
        if (CollectionUtils.isNotEmpty(questions)) {
            int totalScore = 0;
            for (ExamQuestionRequest questionRequest : questions) {
                Question question = questionMapper.selectById(questionRequest.getQuestionId());
                ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");

                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setExamId(exam.getId());
                examQuestion.setQuestionId(questionRequest.getQuestionId());
                examQuestion.setScore(questionRequest.getScore());
                examQuestionMapper.insert(examQuestion);

                totalScore += questionRequest.getScore();
            }
            // 检查总分是否匹配
            if (totalScore != examAddRequest.getTotalScore()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目总分与考试总分不匹配");
            }
        }

        return exam.getId();
    }

    @Override
    public boolean updateExam(ExamEditRequest examEditRequest, HttpServletRequest request) {
        Long id = examEditRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        Exam exam = this.getById(id);
        ThrowUtils.throwIf(exam == null, ErrorCode.NOT_FOUND_ERROR, "考试不存在");

        exam.setExamName(examEditRequest.getExamName());
        exam.setDescription(examEditRequest.getDescription());
        exam.setStartTime(examEditRequest.getStartTime());
        exam.setEndTime(examEditRequest.getEndTime());
        exam.setTotalScore(examEditRequest.getTotalScore());
        validExam(exam, false);

        // 更新考试
        boolean updated = this.updateById(exam);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "考试更新失败");

        // 更新题目关联
        List<ExamQuestionRequest> questions = examEditRequest.getQuestions();
        if (CollectionUtils.isNotEmpty(questions)) {
            // 删除旧的题目关联
            QueryWrapper<ExamQuestion> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("examId", id);
            examQuestionMapper.delete(deleteWrapper);

            // 添加新的题目关联
            int totalScore = 0;
            for (ExamQuestionRequest questionRequest : questions) {
                Question question = questionMapper.selectById(questionRequest.getQuestionId());
                ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");

                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setExamId(id);
                examQuestion.setQuestionId(questionRequest.getQuestionId());
                examQuestion.setScore(questionRequest.getScore());
                examQuestionMapper.insert(examQuestion);

                totalScore += questionRequest.getScore();
            }
            // 检查总分是否匹配
            if (totalScore != examEditRequest.getTotalScore()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目总分与考试总分不匹配");
            }
        }

        return true;
    }

    @Override
    public boolean publishExamToClass(long examId, long classId) {
        // 检查考试是否存在
        Exam exam = this.getById(examId);
        ThrowUtils.throwIf(exam == null, ErrorCode.NOT_FOUND_ERROR, "考试不存在");

        // 检查班级是否存在
        com.mxf.selaboj.model.entity.Class clazz = classMapper.selectById(classId);
        ThrowUtils.throwIf(clazz == null, ErrorCode.NOT_FOUND_ERROR, "班级不存在");

        // 获取班级学生
        QueryWrapper<ClassStudent> classStudentQueryWrapper = new QueryWrapper<>();
        classStudentQueryWrapper.eq("classId", classId);
        classStudentQueryWrapper.eq("isDelete", false);
        List<ClassStudent> classStudents = classStudentMapper.selectList(classStudentQueryWrapper);

        if (CollectionUtils.isNotEmpty(classStudents)) {
            for (ClassStudent classStudent : classStudents) {
                // 检查是否已有考试记录
                QueryWrapper<ExamRecord> examRecordQueryWrapper = new QueryWrapper<>();
                examRecordQueryWrapper.eq("examId", examId);
                examRecordQueryWrapper.eq("userId", classStudent.getUserId());
                examRecordQueryWrapper.eq("isDelete", false);
                if (examRecordMapper.selectCount(examRecordQueryWrapper) == 0) {
                    // 创建考试记录
                    ExamRecord examRecord = new ExamRecord();
                    examRecord.setExamId(examId);
                    examRecord.setUserId(classStudent.getUserId());
                    examRecord.setTotalScore(0);
                    examRecord.setStatus(0); // 待考试
                    examRecordMapper.insert(examRecord);
                }
            }
        }

        return true;
    }

    @Override
    public long startExam(long examId, HttpServletRequest request) {
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);

        // 检查考试是否存在
        Exam exam = this.getById(examId);
        ThrowUtils.throwIf(exam == null, ErrorCode.NOT_FOUND_ERROR, "考试不存在");

        // 检查考试时间
        Date now = new Date();
        if (now.before(exam.getStartTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "考试尚未开始");
        }
        if (now.after(exam.getEndTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "考试已结束");
        }

        // 检查是否已有考试记录（只有被发布到考试的班级学生才有记录）
        QueryWrapper<ExamRecord> examRecordQueryWrapper = new QueryWrapper<>();
        examRecordQueryWrapper.eq("examId", examId);
        examRecordQueryWrapper.eq("userId", loginUser.getId());
        examRecordQueryWrapper.eq("isDelete", false);
        ExamRecord examRecord = examRecordMapper.selectOne(examRecordQueryWrapper);

        if (examRecord == null) {
            // 没有考试记录，说明用户不在考试发布的班级中
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您不在该考试的班级中，无法开始考试");
        } else if (examRecord.getStatus() == 0) {
            // 更新状态为考试中
            examRecord.setStatus(1);
            examRecord.setStartTime(new Date());
            examRecordMapper.updateById(examRecord);
        } else if (examRecord.getStatus() == 1) {
            // 考试中，直接返回已有记录
        } else if (examRecord.getStatus() == 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "考试已完成");
        }

        // 创建答题记录
        QueryWrapper<ExamQuestion> examQuestionQueryWrapper = new QueryWrapper<>();
        examQuestionQueryWrapper.eq("examId", examId);
        examQuestionQueryWrapper.eq("isDelete", false);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionQueryWrapper);

        if (CollectionUtils.isNotEmpty(examQuestions)) {
            for (ExamQuestion examQuestion : examQuestions) {
                // 检查是否已有答题记录
                QueryWrapper<ExamAnswer> examAnswerQueryWrapper = new QueryWrapper<>();
                examAnswerQueryWrapper.eq("examRecordId", examRecord.getId());
                examAnswerQueryWrapper.eq("questionId", examQuestion.getQuestionId());
                examAnswerQueryWrapper.eq("isDelete", false);
                if (examAnswerMapper.selectCount(examAnswerQueryWrapper) == 0) {
                    // 创建答题记录
                    ExamAnswer examAnswer = new ExamAnswer();
                    examAnswer.setExamRecordId(examRecord.getId());
                    examAnswer.setQuestionId(examQuestion.getQuestionId());
                    examAnswer.setScore(0);
                    examAnswer.setStatus(0); // 未作答
                    examAnswerMapper.insert(examAnswer);
                }
            }
        }

        return examRecord.getId();
    }

    @Override
    public boolean submitAnswer(long examRecordId, long questionId, String answer, HttpServletRequest request) {
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);

        // 检查考试记录
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        ThrowUtils.throwIf(examRecord == null, ErrorCode.NOT_FOUND_ERROR, "考试记录不存在");
        ThrowUtils.throwIf(!examRecord.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        ThrowUtils.throwIf(examRecord.getStatus() != 1, ErrorCode.PARAMS_ERROR, "考试状态错误");

        // 检查答题记录
        QueryWrapper<ExamAnswer> examAnswerQueryWrapper = new QueryWrapper<>();
        examAnswerQueryWrapper.eq("examRecordId", examRecordId);
        examAnswerQueryWrapper.eq("questionId", questionId);
        examAnswerQueryWrapper.eq("isDelete", false);
        ExamAnswer examAnswer = examAnswerMapper.selectOne(examAnswerQueryWrapper);
        ThrowUtils.throwIf(examAnswer == null, ErrorCode.NOT_FOUND_ERROR, "答题记录不存在");

        // 获取题目信息
        Question question = questionMapper.selectById(questionId);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");

        // 获取题目分值
        QueryWrapper<ExamQuestion> examQuestionQueryWrapper = new QueryWrapper<>();
        examQuestionQueryWrapper.eq("examId", examRecord.getExamId());
        examQuestionQueryWrapper.eq("questionId", questionId);
        examQuestionQueryWrapper.eq("isDelete", false);
        ExamQuestion examQuestion = examQuestionMapper.selectOne(examQuestionQueryWrapper);
        ThrowUtils.throwIf(examQuestion == null, ErrorCode.NOT_FOUND_ERROR, "考试题目不存在");

        // 更新答案并评分
        examAnswer.setAnswer(answer);

        // 即时评分
        int score = 0;
        int status = 3; // 错误

        // 根据题目类型评分
        if (question.getQuestionType() == 1) {
            // 选择题：直接比较答案
            if (StringUtils.equals(answer, question.getAnswer())) {
                score = examQuestion.getScore();
                status = 2; // 正确
            }
        } else if (question.getQuestionType() == 0) {
            // 编程题：运行代码并比较输出
            try {
                // 解析测试用例
                String judgeCaseStr = question.getJudgeCase();
                if (StringUtils.isNotBlank(judgeCaseStr)) {
                    List<JudgeCase> judgeCases = GSON.fromJson(judgeCaseStr,
                            new TypeToken<List<JudgeCase>>() {
                            }.getType());

                    if (CollectionUtils.isNotEmpty(judgeCases)) {
                        // 准备输入列表
                        List<String> inputList = new ArrayList<>();
                        List<String> expectedOutputs = new ArrayList<>();
                        for (JudgeCase judgeCase : judgeCases) {
                            inputList.add(judgeCase.getInput());
                            expectedOutputs.add(judgeCase.getOutput());
                        }

                        // 调用代码沙箱运行代码
                        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance("javaNative");
                        ExcuteCodeRequest excuteCodeRequest = ExcuteCodeRequest.builder()
                                .code(answer)
                                .language("java")
                                .inputList(inputList)
                                .build();

                        ExcuteCodeResponse response = codeSandbox.excuteCode(excuteCodeRequest);

                        if (response.getStatus() == 2) {
                            // 运行成功，比较输出
                            List<String> actualOutputs = response.getOutputList();
                            boolean allCorrect = true;

                            for (int i = 0; i < expectedOutputs.size(); i++) {
                                String expected = expectedOutputs.get(i).trim();
                                String actual = actualOutputs.get(i).trim();
                                if (!expected.equals(actual)) {
                                    allCorrect = false;
                                    break;
                                }
                            }

                            if (allCorrect) {
                                score = examQuestion.getScore();
                                status = 2; // 正确
                            }
                        } else {
                            // 运行失败
                            log.error("代码运行失败: {}", response.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("编程题评分失败", e);
            }
        }

        examAnswer.setScore(score);
        examAnswer.setStatus(status);
        examAnswerMapper.updateById(examAnswer);

        // 更新考试记录总分
        QueryWrapper<ExamAnswer> scoreQueryWrapper = new QueryWrapper<>();
        scoreQueryWrapper.eq("examRecordId", examRecordId);
        scoreQueryWrapper.eq("isDelete", false);
        List<ExamAnswer> allAnswers = examAnswerMapper.selectList(scoreQueryWrapper);

        int totalScore = 0;
        for (ExamAnswer ans : allAnswers) {
            totalScore += ans.getScore();
        }

        examRecord.setTotalScore(totalScore);
        examRecordMapper.updateById(examRecord);

        return true;
    }

    @Override
    public boolean endExam(long examRecordId, HttpServletRequest request) {
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);

        // 检查考试记录
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        ThrowUtils.throwIf(examRecord == null, ErrorCode.NOT_FOUND_ERROR, "考试记录不存在");
        ThrowUtils.throwIf(!examRecord.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        ThrowUtils.throwIf(examRecord.getStatus() != 1, ErrorCode.PARAMS_ERROR, "考试状态错误");

        // 自动评分
        autoGrade(examRecordId);

        // 更新状态
        examRecord.setStatus(2); // 已完成
        examRecord.setEndTime(new Date());
        examRecordMapper.updateById(examRecord);

        return true;
    }

    @Override
    public ExamRecordVO getExamRecord(long examRecordId, HttpServletRequest request) {
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);

        // 检查考试记录
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        ThrowUtils.throwIf(examRecord == null, ErrorCode.NOT_FOUND_ERROR, "考试记录不存在");

        // 检查权限（只能查看自己的考试记录）
        if (!examRecord.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看");
        }

        ExamRecordVO examRecordVO = ExamRecordVO.objToVo(examRecord);

        // 获取考试信息
        Exam exam = this.getById(examRecord.getExamId());
        if (exam != null) {
            examRecordVO.setExamName(exam.getExamName());
        }

        // 获取用户信息
        User user = userService.getById(examRecord.getUserId());
        if (user != null) {
            examRecordVO.setUserName(user.getUserName());
        }

        // 获取答题记录
        QueryWrapper<ExamAnswer> examAnswerQueryWrapper = new QueryWrapper<>();
        examAnswerQueryWrapper.eq("examRecordId", examRecordId);
        examAnswerQueryWrapper.eq("isDelete", false);
        List<ExamAnswer> examAnswers = examAnswerMapper.selectList(examAnswerQueryWrapper);

        if (CollectionUtils.isNotEmpty(examAnswers)) {
            List<ExamAnswerVO> answerVOs = new ArrayList<>();
            for (ExamAnswer examAnswer : examAnswers) {
                ExamAnswerVO answerVO = ExamAnswerVO.objToVo(examAnswer);

                // 获取题目信息
                Question question = questionMapper.selectById(examAnswer.getQuestionId());
                if (question != null) {
                    answerVO.setQuestionTitle(question.getTitle());
                    answerVO.setQuestionType(question.getQuestionType());
                    answerVO.setCorrectAnswer(question.getAnswer());
                }

                // 获取题目分值
                QueryWrapper<ExamQuestion> examQuestionQueryWrapper = new QueryWrapper<>();
                examQuestionQueryWrapper.eq("examId", examRecord.getExamId());
                examQuestionQueryWrapper.eq("questionId", examAnswer.getQuestionId());
                examQuestionQueryWrapper.eq("isDelete", false);
                ExamQuestion examQuestion = examQuestionMapper.selectOne(examQuestionQueryWrapper);
                if (examQuestion != null) {
                    answerVO.setQuestionScore(examQuestion.getScore());
                }

                answerVOs.add(answerVO);
            }
            examRecordVO.setAnswers(answerVOs);
        }

        return examRecordVO;
    }

    @Override
    public List<ExamRecordVO> getUserExamRecords(long examId, HttpServletRequest request) {
        // 检查考试是否存在
        Exam exam = this.getById(examId);
        ThrowUtils.throwIf(exam == null, ErrorCode.NOT_FOUND_ERROR, "考试不存在");

        // 获取用户的考试记录
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<ExamRecord> examRecordQueryWrapper = new QueryWrapper<>();
        examRecordQueryWrapper.eq("examId", examId);
        examRecordQueryWrapper.eq("userId", loginUser.getId());
        examRecordQueryWrapper.eq("isDelete", false);
        List<ExamRecord> examRecords = examRecordMapper.selectList(examRecordQueryWrapper);

        if (CollectionUtils.isEmpty(examRecords)) {
            return new ArrayList<>();
        }

        // 转换为VO
        List<ExamRecordVO> examRecordVOs = new ArrayList<>();
        for (ExamRecord examRecord : examRecords) {
            ExamRecordVO examRecordVO = getExamRecord(examRecord.getId(), request);
            examRecordVOs.add(examRecordVO);
        }

        return examRecordVOs;
    }

    @Override
    public boolean autoGrade(long examRecordId) {
        // 检查考试记录
        ExamRecord examRecord = examRecordMapper.selectById(examRecordId);
        ThrowUtils.throwIf(examRecord == null, ErrorCode.NOT_FOUND_ERROR, "考试记录不存在");

        // 获取答题记录
        QueryWrapper<ExamAnswer> examAnswerQueryWrapper = new QueryWrapper<>();
        examAnswerQueryWrapper.eq("examRecordId", examRecordId);
        examAnswerQueryWrapper.eq("isDelete", false);
        List<ExamAnswer> examAnswers = examAnswerMapper.selectList(examAnswerQueryWrapper);

        if (CollectionUtils.isEmpty(examAnswers)) {
            return true;
        }

        int totalScore = 0;
        for (ExamAnswer examAnswer : examAnswers) {
            // 获取题目信息
            Question question = questionMapper.selectById(examAnswer.getQuestionId());
            if (question == null) {
                continue;
            }

            // 获取题目分值
            QueryWrapper<ExamQuestion> examQuestionQueryWrapper = new QueryWrapper<>();
            examQuestionQueryWrapper.eq("examId", examRecord.getExamId());
            examQuestionQueryWrapper.eq("questionId", examAnswer.getQuestionId());
            examQuestionQueryWrapper.eq("isDelete", false);
            ExamQuestion examQuestion = examQuestionMapper.selectOne(examQuestionQueryWrapper);
            if (examQuestion == null) {
                continue;
            }

            // 自动评分
            int score = 0;
            int status = 3; // 错误

            if (StringUtils.equals(examAnswer.getAnswer(), question.getAnswer())) {
                score = examQuestion.getScore();
                status = 2; // 正确
            }

            // 更新答题记录
            examAnswer.setScore(score);
            examAnswer.setStatus(status);
            examAnswerMapper.updateById(examAnswer);

            totalScore += score;
        }

        // 更新考试记录总分
        examRecord.setTotalScore(totalScore);
        examRecordMapper.updateById(examRecord);

        return true;
    }

    @Override
    public boolean removeById(Long id) {
        // 删除答题记录（需要先删除exam_answer，因为它依赖exam_record）
        QueryWrapper<ExamRecord> examRecordQueryWrapper = new QueryWrapper<>();
        examRecordQueryWrapper.eq("examId", id);
        List<ExamRecord> examRecords = examRecordMapper.selectList(examRecordQueryWrapper);
        if (CollectionUtils.isNotEmpty(examRecords)) {
            for (ExamRecord examRecord : examRecords) {
                QueryWrapper<ExamAnswer> examAnswerQueryWrapper = new QueryWrapper<>();
                examAnswerQueryWrapper.eq("examRecordId", examRecord.getId());
                examAnswerMapper.delete(examAnswerQueryWrapper);
            }
        }

        // 删除考试记录
        QueryWrapper<ExamRecord> recordDeleteWrapper = new QueryWrapper<>();
        recordDeleteWrapper.eq("examId", id);
        examRecordMapper.delete(recordDeleteWrapper);

        // 删除考试题目关联
        QueryWrapper<ExamQuestion> examQuestionQueryWrapper = new QueryWrapper<>();
        examQuestionQueryWrapper.eq("examId", id);
        examQuestionMapper.delete(examQuestionQueryWrapper);

        // 删除考试本身
        return super.removeById(id);
    }
}
