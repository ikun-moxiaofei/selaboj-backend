<template>
  <div class="question-edit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>编辑题目</h2>
        </div>
      </template>
      
      <el-form :model="questionForm" :rules="rules" ref="questionFormRef" label-width="100px">
        <el-form-item label="题目类型" prop="questionType">
          <el-radio-group v-model="questionForm.questionType">
            <el-radio-button :label="0">编程题</el-radio-button>
            <el-radio-button :label="1">选择题</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="题目名称" prop="title">
          <el-input v-model="questionForm.title" placeholder="请输入题目名称" />
        </el-form-item>
        
        <el-form-item label="题目描述" prop="content">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            rows="6"
            placeholder="请输入题目描述"
          />
        </el-form-item>
        
        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="questionForm.tags"
            multiple
            placeholder="请选择标签"
            style="width: 100%"
          >
            <el-option label="Java" value="Java" />
            <el-option label="Python" value="Python" />
            <el-option label="C++" value="C++" />
            <el-option label="算法" value="算法" />
            <el-option label="数据结构" value="数据结构" />
            <el-option label="选择题" value="选择题" />
            <el-option label="编程题" value="编程题" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="正确答案" prop="answer">
          <el-input v-model="questionForm.answer" placeholder="请输入正确答案" />
        </el-form-item>
        
        <!-- 选择题选项 -->
        <el-collapse v-if="questionForm.questionType === 1">
          <el-collapse-item title="选择题选项">
            <div v-for="(option, index) in questionForm.options" :key="index" class="option-item">
              <el-form-item :label="`选项${String.fromCharCode(65 + index)}`">
                <el-input v-model="option.value" placeholder="请输入选项内容" />
                <el-button type="danger" size="small" @click="removeOption(index)" style="margin-top: 10px">删除选项</el-button>
              </el-form-item>
            </div>
            <el-button type="primary" @click="addOption">添加选项</el-button>
          </el-collapse-item>
        </el-collapse>
        
        <!-- 编程题配置 -->
        <el-collapse v-if="questionForm.questionType === 0">
          <el-collapse-item title="判题用例">
            <div v-for="(judgeCase, index) in questionForm.judgeCase" :key="index" class="judge-case-item">
              <el-form-item :label="`用例${index + 1}`">
                <el-input v-model="judgeCase.input" placeholder="请输入输入" />
                <el-input v-model="judgeCase.output" placeholder="请输入预期输出" style="margin-top: 10px" />
                <el-button type="danger" size="small" @click="removeJudgeCase(index)" style="margin-top: 10px">删除用例</el-button>
              </el-form-item>
            </div>
            <el-button type="primary" @click="addJudgeCase">添加用例</el-button>
          </el-collapse-item>
          
          <el-collapse-item title="判题配置">
            <el-form-item label="时间限制(ms)">
              <el-input-number v-model="questionForm.judgeConfig.timeLimit" :min="100" :max="10000" />
            </el-form-item>
            <el-form-item label="内存限制(KB)">
              <el-input-number v-model="questionForm.judgeConfig.memoryLimit" :min="1024" :max="1024000" />
            </el-form-item>
            <el-form-item label="堆栈限制(KB)">
              <el-input-number v-model="questionForm.judgeConfig.stackLimit" :min="1024" :max="1024000" />
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import questionApi from '../../api/question'

const router = useRouter()
const route = useRoute()
const questionFormRef = ref(null)
const loading = ref(false)
const questionId = ref(route.params.id)

const questionForm = reactive({
  id: '',
  questionType: 0,
  title: '',
  content: '',
  tags: [],
  answer: '',
  options: [
    { key: 'A', value: '' },
    { key: 'B', value: '' },
    { key: 'C', value: '' },
    { key: 'D', value: '' }
  ],
  judgeCase: [
    { input: '', output: '' }
  ],
  judgeConfig: {
    timeLimit: 1000,
    memoryLimit: 102400,
    stackLimit: 102400
  }
})

const rules = {
  questionType: [
    { required: true, message: '请选择题目类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入题目名称', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入题目描述', trigger: 'blur' }
  ],
  tags: [
    { required: true, message: '请至少选择一个标签', trigger: 'blur' }
  ],
  answer: [
    { required: true, message: '请输入正确答案', trigger: 'blur' }
  ]
}

const fetchQuestionDetail = async () => {
  try {
    const response = await questionApi.getQuestion(questionId.value)
    const data = response.data
    questionForm.id = data.id
    questionForm.questionType = data.questionType
    questionForm.title = data.title
    questionForm.content = data.content
    questionForm.tags = JSON.parse(data.tags)
    questionForm.answer = data.answer
    
    // 处理选择题选项
    if (data.options) {
      questionForm.options = JSON.parse(data.options)
    }
    
    // 处理编程题配置
    if (data.judgeCase) {
      questionForm.judgeCase = JSON.parse(data.judgeCase)
    }
    if (data.judgeConfig) {
      questionForm.judgeConfig = JSON.parse(data.judgeConfig)
    }
  } catch (error) {
    console.error('获取题目详情失败', error)
  }
}

const addOption = () => {
  const key = String.fromCharCode(65 + questionForm.options.length)
  questionForm.options.push({ key, value: '' })
}

const removeOption = (index) => {
  questionForm.options.splice(index, 1)
}

const addJudgeCase = () => {
  questionForm.judgeCase.push({ input: '', output: '' })
}

const removeJudgeCase = (index) => {
  questionForm.judgeCase.splice(index, 1)
}

const handleSubmit = async () => {
  if (!questionFormRef.value) return
  
  await questionFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await questionApi.editQuestion(questionForm)
        ElMessage.success('编辑成功')
        router.back()
      } catch (error) {
        ElMessage.error('编辑失败')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchQuestionDetail()
})
</script>

<style scoped>
.question-edit-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.option-item,
.judge-case-item {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}
</style>
