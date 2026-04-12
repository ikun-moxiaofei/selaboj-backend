<template>
  <div class="exam-detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ examInfo.examName }}</h2>
          <el-button v-if="!isExamStarted" type="primary" @click="handleStartExam">开始考试</el-button>
          <el-button v-else-if="isExamStarted && !isExamEnded" type="danger" @click="handleEndExam">结束考试</el-button>
        </div>
      </template>
      
      <div class="exam-info">
        <el-descriptions :column="2">
          <el-descriptions-item label="考试描述">{{ examInfo.description }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ examInfo.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ examInfo.endTime }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ examInfo.totalScore }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div v-if="isExamStarted" class="exam-questions">
        <div class="score-display">
          <h3>考试题目</h3>
          <div class="current-score">
            <span>当前得分：</span>
            <span class="score-value">{{ examRecord.totalScore || 0 }}</span>
            <span> / {{ examInfo.totalScore }}</span>
          </div>
        </div>
        <div v-for="(question, index) in examQuestions" :key="question.questionId" class="question-item">
          <div class="question-header">
            <span class="question-number">{{ index + 1 }}. </span>
            <span class="question-title">{{ question.questionTitle }}</span>
            <span class="question-score">({{ question.score }}分)</span>
            <el-tag v-if="question.answerStatus === 2" type="success" size="small" style="margin-left: 10px">正确 +{{ question.answerScore || 0 }}分</el-tag>
            <el-tag v-else-if="question.answerStatus === 3" type="danger" size="small" style="margin-left: 10px">错误</el-tag>
            <el-tag v-else-if="question.answerStatus === 1" type="info" size="small" style="margin-left: 10px">已提交</el-tag>
          </div>
          
          <!-- 选择题 -->
          <div v-if="question.questionType === 1" class="question-content">
            <div v-for="option in question.options" :key="option.key" class="option-item">
              <el-radio :label="option.key" v-model="question.userAnswer">{{ option.key }}. {{ option.value }}</el-radio>
            </div>
          </div>
          
          <!-- 编程题 -->
          <div v-else class="question-content">
            <el-form :model="question" :rules="{}" ref="codeFormRef">
              <el-form-item label="代码">
                <el-input
                  v-model="question.userAnswer"
                  type="textarea"
                  rows="10"
                  placeholder="请输入代码"
                />
              </el-form-item>
              <el-form-item label="编程语言">
                <el-select v-model="question.language" placeholder="请选择编程语言">
                  <el-option label="Java" value="java" />
                  <el-option label="Python" value="python" />
                  <el-option label="C++" value="cpp" />
                </el-select>
              </el-form-item>
            </el-form>
            <div class="code-actions">
              <el-button type="info" @click="handleRunCode(question)" :loading="question.isRunning">运行测试</el-button>
              <el-button type="primary" @click="handleSubmitAnswer(question.questionId, question.userAnswer)" :disabled="question.answerStatus === 2">提交答案</el-button>
            </div>
            <div v-if="question.runResult" class="run-result">
              <h4>运行结果：</h4>
              <pre>{{ question.runResult }}</pre>
            </div>
          </div>
          
          <el-button v-if="question.questionType === 1" type="primary" @click="handleSubmitAnswer(question.questionId, question.userAnswer)" :disabled="question.answerStatus === 2">提交答案</el-button>
        </div>
      </div>
      
      <div v-else-if="isExamEnded" class="exam-result">
        <h3>考试结果</h3>
        <el-descriptions :column="2">
          <el-descriptions-item label="总分">{{ examInfo.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ examRecord.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="考试状态" :span="2">
            <el-tag v-if="examRecord.status === 2" type="success">已完成</el-tag>
            <el-tag v-else-if="examRecord.status === 1" type="warning">进行中</el-tag>
            <el-tag v-else type="info">未开始</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="answer-records">
          <h4>答题记录</h4>
          <el-table :data="examRecord.answers" style="width: 100%">
            <el-table-column prop="questionId" label="题目ID" width="80" />
            <el-table-column prop="answer" label="你的答案" min-width="200" />
            <el-table-column prop="score" label="得分" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 2" type="success">正确</el-tag>
                <el-tag v-else-if="scope.row.status === 1" type="info">已作答</el-tag>
                <el-tag v-else-if="scope.row.status === 0" type="warning">未作答</el-tag>
                <el-tag v-else type="danger">错误</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <div v-else class="exam-not-started">
        <el-empty description="考试尚未开始" />
        <p>请点击上方的"开始考试"按钮开始考试。</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import examApi from '../../api/exam'
import questionApi from '../../api/question'

const route = useRoute()
const examId = ref(route.params.id)
const examInfo = ref({
  id: '',
  examName: '',
  description: '',
  startTime: '',
  endTime: '',
  totalScore: 0
})
const examQuestions = ref([])
const examRecord = ref({
  id: '',
  examId: '',
  userId: '',
  totalScore: 0,
  status: 0,
  answers: []
})

const isExamStarted = ref(false)
const isExamEnded = ref(false)

const fetchExamDetail = async () => {
  try {
    const response = await examApi.getExamVO(examId.value)
    examInfo.value = response.data
  } catch (error) {
    console.error('获取考试详情失败', error)
  }
}

const fetchExamRecord = async () => {
  try {
    // 这里需要根据用户ID获取考试记录
    const response = await examApi.getUserExamRecords(examId.value)
    if (response.data && response.data.length > 0) {
      examRecord.value = response.data[0]
      // 更新考试状态
      if (examRecord.value.status === 1) {
        isExamStarted.value = true
      } else if (examRecord.value.status === 2) {
        isExamStarted.value = true
        isExamEnded.value = true
      }
    } else {
      // 没有考试记录
      examRecord.value = {
        id: '',
        examId: examId.value,
        userId: '',
        totalScore: 0,
        status: 0,
        answers: []
      }
    }
  } catch (error) {
    console.error('获取考试记录失败', error)
    ElMessage.error('获取考试记录失败')
  }
}

const handleStartExam = async () => {
  try {
    const response = await examApi.startExam(examId.value)
    examRecord.value.id = response.data
    ElMessage.success('开始考试成功')
    isExamStarted.value = true
    await fetchExamQuestions()
    await updateQuestionStatus()
  } catch (error) {
    console.error('开始考试失败', error)
    ElMessage.error('开始考试失败')
  }
}

const fetchExamQuestions = async () => {
  try {
    // 从考试详情中获取题目列表
    const response = await examApi.getExamVO(examId.value)
    console.log('获取考试详情响应:', response)
    if (response.data && response.data.questions) {
      console.log('题目列表:', response.data.questions)
      examQuestions.value = response.data.questions.map(question => ({
        ...question,
        userAnswer: '',
        language: 'java' // 默认语言
      }))
      console.log('处理后题目列表:', examQuestions.value)
    }
  } catch (error) {
    console.error('获取考试题目失败', error)
    ElMessage.error('获取考试题目失败')
  }
}

const handleSubmitAnswer = async (questionId, answer) => {
  try {
    await examApi.submitAnswer(examRecord.value.id, questionId, answer)
    ElMessage.success('提交答案成功')
    // 重新获取考试记录，更新得分显示
    await fetchExamRecord()
    // 更新题目状态
    await updateQuestionStatus()
  } catch (error) {
    console.error('提交答案失败', error)
    ElMessage.error('提交答案失败')
  }
}

const handleRunCode = async (question) => {
  if (!question.userAnswer || question.userAnswer.trim() === '') {
    ElMessage.warning('请先输入代码')
    return
  }
  
  question.isRunning = true
  question.runResult = null
  
  try {
    // 获取题目的测试用例
    const questionResponse = await questionApi.getQuestion(question.questionId)
    const questionData = questionResponse.data
    
    if (!questionData.judgeCase) {
      ElMessage.warning('该题目没有测试用例')
      return
    }
    
    // 解析测试用例
    const testCases = JSON.parse(questionData.judgeCase)
    const inputList = testCases.map(tc => tc.input)
    const expectedOutputs = testCases.map(tc => tc.output)
    
    // 调用代码沙箱运行代码
    const response = await fetch('/api/code/run', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        code: question.userAnswer,
        language: question.language || 'java',
        inputList: inputList
      })
    })
    
    const result = await response.json()
    
    if (result.code === 0) {
      const outputs = result.data.outputList
      let runResultText = '测试用例运行结果：\n\n'
      
      testCases.forEach((testCase, index) => {
        const actualOutput = outputs[index] || ''
        const expectedOutput = expectedOutputs[index]
        const isCorrect = actualOutput.trim() === expectedOutput.trim()
        
        runResultText += `测试用例 ${index + 1}:\n`
        runResultText += `输入: ${testCase.input}\n`
        runResultText += `预期输出: ${expectedOutput}\n`
        runResultText += `实际输出: ${actualOutput}\n`
        runResultText += `结果: ${isCorrect ? '✓ 通过' : '✗ 失败'}\n\n`
      })
      
      question.runResult = runResultText
    } else {
      question.runResult = '运行失败: ' + result.message
    }
  } catch (error) {
    console.error('运行代码失败', error)
    question.runResult = '运行失败: ' + error.message
  } finally {
    question.isRunning = false
  }
}

const updateQuestionStatus = async () => {
  try {
    // 获取考试记录
    const response = await examApi.getExamRecord(examRecord.value.id)
    if (response.data && response.data.answers) {
      // 更新每道题的答题状态和得分
      examQuestions.value.forEach(question => {
        const answerRecord = response.data.answers.find(a => a.questionId === question.questionId)
        if (answerRecord) {
          question.answerStatus = answerRecord.status
          question.answerScore = answerRecord.score
        }
      })
    }
  } catch (error) {
    console.error('更新题目状态失败', error)
  }
}

const handleEndExam = async () => {
  ElMessageBox.confirm('确定要结束考试吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await examApi.endExam(examRecord.value.id)
      ElMessage.success('结束考试成功')
      isExamEnded.value = true
      // 重新获取考试记录
      fetchExamRecord()
    } catch (error) {
      ElMessage.error('结束考试失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchExamDetail()
  fetchExamRecord()
})
</script>

<style scoped>
.exam-detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exam-info {
  margin: 20px 0;
}

.exam-questions {
  margin-top: 30px;
}

.score-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.current-score {
  font-size: 18px;
  font-weight: bold;
}

.score-value {
  color: #409eff;
  font-size: 24px;
  margin: 0 5px;
}

.exam-questions h3 {
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: bold;
}

.question-item {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.question-header {
  font-weight: bold;
  margin-bottom: 15px;
  font-size: 16px;
}

.question-number {
  font-weight: bold;
}

.question-score {
  color: #f56c6c;
  margin-left: 10px;
}

.question-content {
  margin-bottom: 20px;
}

.code-actions {
  margin-top: 15px;
  margin-bottom: 15px;
}

.code-actions .el-button {
  margin-right: 10px;
}

.run-result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.run-result h4 {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: bold;
}

.run-result pre {
  margin: 0;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.option-item {
  margin-bottom: 10px;
}

.exam-result {
  margin-top: 30px;
}

.exam-result h3 {
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: bold;
}

.answer-records {
  margin-top: 30px;
}

.answer-records h4 {
  margin-bottom: 15px;
  font-size: 14px;
  font-weight: bold;
}

.exam-not-started {
  text-align: center;
  padding: 50px 0;
}

.exam-not-started p {
  margin-top: 20px;
  color: #606266;
}
</style>
