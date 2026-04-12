<template>
  <div class="exam-add-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>创建考试</h2>
        </div>
      </template>
      
      <el-form :model="examForm" :rules="rules" ref="examFormRef" label-width="100px">
        <el-form-item label="考试名称" prop="examName">
          <el-input v-model="examForm.examName" placeholder="请输入考试名称" />
        </el-form-item>
        
        <el-form-item label="考试描述" prop="description">
          <el-input
            v-model="examForm.description"
            type="textarea"
            rows="4"
            placeholder="请输入考试描述"
          />
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="examForm.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="examForm.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="examForm.totalScore" :min="1" :max="1000" />
        </el-form-item>
        
        <!-- 题目选择 -->
        <el-form-item label="题目选择" prop="questions">
          <el-collapse>
            <el-collapse-item title="选择题目">
              <el-input v-model="questionSearch" placeholder="搜索题目" style="margin-bottom: 15px" />
              <el-button type="primary" @click="searchQuestions">搜索</el-button>
              
              <el-table ref="tableRef" :data="questionList" style="width: 100%; margin-top: 15px" @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="题目ID" width="80" />
                <el-table-column prop="title" label="题目名称" min-width="200" />
                <el-table-column prop="questionType" label="题目类型" width="100">
                  <template #default="scope">
                    <el-tag v-if="scope.row.questionType === 0" size="small">编程题</el-tag>
                    <el-tag v-else type="success" size="small">选择题</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="分值" width="100">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.score" :min="1" :max="100" />
                  </template>
                </el-table-column>
              </el-table>
              
              <el-button type="success" @click="addSelectedQuestions" style="margin-top: 15px">添加选中题目</el-button>
            </el-collapse-item>
          </el-collapse>
          
          <!-- 已选择题目 -->
          <div class="selected-questions" style="margin-top: 20px">
            <h4>已选择题目</h4>
            <el-table :data="examForm.questions" style="width: 100%">
              <el-table-column prop="questionId" label="题目ID" width="80" />
              <el-table-column prop="questionTitle" label="题目名称" min-width="200" />
              <el-table-column prop="score" label="分值" width="100" />
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button type="danger" size="small" @click="removeQuestion(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
        
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
import { useRouter } from 'vue-router'
import examApi from '../../api/exam'
import questionApi from '../../api/question'

const router = useRouter()
const examFormRef = ref(null)
const loading = ref(false)

const examForm = reactive({
  examName: '',
  description: '',
  startTime: '',
  endTime: '',
  totalScore: 100,
  questions: []
})

const questionList = ref([])
const questionSearch = ref('')
const selectedQuestions = ref([])
const tableRef = ref(null)

const rules = {
  examName: [
    { required: true, message: '请输入考试名称', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入考试描述', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'blur' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'blur' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'blur' }
  ],
  questions: [
    { required: true, message: '请至少选择一道题目', trigger: 'blur' }
  ]
}

const searchQuestions = async () => {
  try {
    console.log('搜索题目，参数:', {
      current: 1,
      pageSize: 20,
      title: questionSearch.value
    })
    const response = await questionApi.listQuestionVOByPage({
      current: 1,
      pageSize: 20,
      title: questionSearch.value
    })
    console.log('搜索题目响应:', response)
    questionList.value = response.data.records.map(item => ({
      ...item,
      score: 10 // 默认分值
    }))
    console.log('处理后题目列表:', questionList.value)
  } catch (error) {
    console.error('搜索题目失败', error)
  }
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const addSelectedQuestions = () => {
  selectedQuestions.value.forEach(item => {
    examForm.questions.push({
      questionId: item.id,
      questionTitle: item.title,
      score: item.score
    })
  })
}

const removeQuestion = (index) => {
  examForm.questions.splice(index, 1)
}

const handleSubmit = async () => {
  if (!examFormRef.value) return
  
  await examFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await examApi.addExam(examForm)
        ElMessage.success('创建成功')
        router.back()
      } catch (error) {
        ElMessage.error('创建失败')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  searchQuestions()
})
</script>

<style scoped>
.exam-add-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-questions {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.selected-questions h4 {
  margin-bottom: 15px;
  font-size: 14px;
  font-weight: bold;
}
</style>
