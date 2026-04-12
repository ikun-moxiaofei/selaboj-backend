<template>
  <div class="question-detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ question.title }}</h2>
          <el-tag v-if="question.questionType === 0">编程题</el-tag>
          <el-tag v-else type="success">选择题</el-tag>
        </div>
      </template>
      
      <div class="question-meta">
        <span>提交数: {{ question.submitNum }}</span>
        <span>通过数: {{ question.passNum }}</span>
        <span>创建时间: {{ question.createTime }}</span>
      </div>
      
      <div class="question-tags">
        <el-tag v-for="tag in question.tags" :key="tag" size="small">{{ tag }}</el-tag>
      </div>
      
      <div class="question-content">
        <h3>题目描述</h3>
        <div v-html="question.content"></div>
      </div>
      
      <!-- 选择题选项 -->
      <div v-if="question.questionType === 1" class="question-options">
        <h3>选项</h3>
        <div v-for="option in question.options" :key="option.key" class="option-item">
          <el-radio :label="option.key" v-model="selectedAnswer">{{ option.key }}. {{ option.value }}</el-radio>
        </div>
      </div>
      
      <!-- 编程题配置 -->
      <div v-if="question.questionType === 0" class="question-program">
        <h3>判题配置</h3>
        <el-descriptions :column="3">
          <el-descriptions-item label="时间限制">{{ question.judgeConfig?.timeLimit }}ms</el-descriptions-item>
          <el-descriptions-item label="内存限制">{{ question.judgeConfig?.memoryLimit }}KB</el-descriptions-item>
          <el-descriptions-item label="堆栈限制">{{ question.judgeConfig?.stackLimit }}KB</el-descriptions-item>
        </el-descriptions>
        
        <h3>判题用例</h3>
        <div v-for="(judgeCase, index) in question.judgeCase" :key="index" class="judge-case-item">
          <div class="case-header">用例 {{ index + 1 }}</div>
          <div class="case-content">
            <div class="case-input"><strong>输入:</strong> {{ judgeCase.input }}</div>
            <div class="case-output"><strong>输出:</strong> {{ judgeCase.output }}</div>
          </div>
        </div>
      </div>
      
      <!-- 提交代码（编程题） -->
      <div v-if="question.questionType === 0" class="code-submit">
        <h3>提交代码</h3>
        <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef">
          <el-form-item label="编程语言">
            <el-select v-model="submitForm.language" placeholder="请选择编程语言">
              <el-option label="Java" value="java" />
              <el-option label="Python" value="python" />
              <el-option label="C++" value="cpp" />
            </el-select>
          </el-form-item>
          <el-form-item label="代码">
            <el-input
              v-model="submitForm.code"
              type="textarea"
              rows="10"
              placeholder="请输入代码"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmitCode">提交</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 提交答案（选择题） -->
      <div v-if="question.questionType === 1" class="answer-submit">
        <h3>提交答案</h3>
        <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef">
          <el-form-item label="你的答案" prop="answer">
            <el-select v-model="submitForm.answer" placeholder="请选择答案">
              <el-option v-for="option in question.options" :key="option.key" :label="option.key + '. ' + option.value" :value="option.key" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmitAnswer">提交</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import questionApi from '../../api/question'

const route = useRoute()
const questionId = ref(route.params.id)
const question = ref({
  id: '',
  title: '',
  content: '',
  tags: [],
  questionType: 0,
  submitNum: 0,
  passNum: 0,
  createTime: '',
  options: [],
  judgeCase: [],
  judgeConfig: {}
})

const selectedAnswer = ref('')
const submitForm = reactive({
  language: 'java',
  code: '',
  answer: ''
})

const submitRules = {
  language: [
    { required: true, message: '请选择编程语言', trigger: 'change' }
  ],
  code: [
    { required: true, message: '请输入代码', trigger: 'blur' }
  ],
  answer: [
    { required: true, message: '请选择答案', trigger: 'blur' }
  ]
}

const fetchQuestionDetail = async () => {
  try {
    const response = await questionApi.getQuestionVO(questionId.value)
    const data = response.data
    question.value = {
      id: data.id,
      title: data.title,
      content: data.content,
      tags: data.tags,
      questionType: data.questionType,
      submitNum: data.submitNum,
      passNum: data.passNum,
      createTime: data.createTime,
      options: data.options || [],
      judgeCase: data.judgeCase || [],
      judgeConfig: data.judgeConfig || {}
    }
  } catch (error) {
    console.error('获取题目详情失败', error)
  }
}

const handleSubmitCode = async () => {
  // 这里应该调用代码提交API
  ElMessage.success('代码提交成功')
}

const handleSubmitAnswer = async () => {
  // 这里应该调用答案提交API
  ElMessage.success('答案提交成功')
}

onMounted(() => {
  fetchQuestionDetail()
})
</script>

<style scoped>
.question-detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-meta {
  margin: 15px 0;
  color: #606266;
  font-size: 14px;
}

.question-meta span {
  margin-right: 20px;
}

.question-tags {
  margin: 15px 0;
}

.question-tags el-tag {
  margin-right: 10px;
}

.question-content {
  margin: 20px 0;
  line-height: 1.6;
}

.question-content h3 {
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: bold;
}

.question-options {
  margin: 20px 0;
}

.question-options h3 {
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: bold;
}

.option-item {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.question-program {
  margin: 20px 0;
}

.question-program h3 {
  margin: 15px 0 10px 0;
  font-size: 16px;
  font-weight: bold;
}

.judge-case-item {
  margin: 10px 0;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.case-header {
  font-weight: bold;
  margin-bottom: 5px;
}

.case-content {
  line-height: 1.5;
}

.code-submit,
.answer-submit {
  margin: 20px 0;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.code-submit h3,
.answer-submit h3 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
}
</style>
