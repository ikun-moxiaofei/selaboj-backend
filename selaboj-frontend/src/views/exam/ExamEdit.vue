<template>
  <div class="exam-edit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>编辑考试</h2>
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
        <el-form-item label="题目选择" prop="examQuestions">
          <el-collapse>
            <el-collapse-item title="选择题目">
              <el-input v-model="questionSearch" placeholder="搜索题目" style="margin-bottom: 15px" />
              <el-button type="primary" @click="searchQuestions">搜索</el-button>
              
              <el-table :data="questionList" style="width: 100%; margin-top: 15px">
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
            <el-table :data="examForm.examQuestions" style="width: 100%">
              <el-table-column prop="questionId" label="题目ID" width="80" />
              <el-table-column prop="questionTitle" label="题目名称" min-width="200" />
              <el-table-column prop="score" label="分值" width="100">
                <template #default="scope">
                  <el-input-number v-model="scope.row.score" :min="1" :max="100" />
                </template>
              </el-table-column>
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
import { useRouter, useRoute } from 'vue-router'
import examApi from '../../api/exam'
import questionApi from '../../api/question'

const router = useRouter()
const route = useRoute()
const examFormRef = ref(null)
const loading = ref(false)
const examId = ref(route.params.id)

const examForm = reactive({
  id: '',
  examName: '',
  description: '',
  startTime: '',
  endTime: '',
  totalScore: 100,
  examQuestions: []
})

const questionList = ref([])
const questionSearch = ref('')

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
  examQuestions: [
    { required: true, message: '请至少选择一道题目', trigger: 'blur' }
  ]
}

const fetchExamDetail = async () => {
  try {
    const response = await examApi.getExam(examId.value)
    const data = response.data
    examForm.id = data.id
    examForm.examName = data.examName
    examForm.description = data.description
    examForm.startTime = data.startTime
    examForm.endTime = data.endTime
    examForm.totalScore = data.totalScore
    if (data.examQuestions) {
      examForm.examQuestions = JSON.parse(data.examQuestions)
    }
  } catch (error) {
    console.error('获取考试详情失败', error)
  }
}

const searchQuestions = async () => {
  try {
    const response = await questionApi.listQuestionVOByPage({
      current: 1,
      pageSize: 50,
      title: questionSearch.value
    })
    questionList.value = response.data.records.map(item => ({
      ...item,
      score: 10 // 默认分值
    }))
  } catch (error) {
    console.error('搜索题目失败', error)
  }
}

const addSelectedQuestions = () => {
  const selected = questionList.value.filter(item => item.__v_isSelected)
  selected.forEach(item => {
    examForm.examQuestions.push({
      questionId: item.id,
      questionTitle: item.title,
      score: item.score
    })
  })
}

const removeQuestion = (index) => {
  examForm.examQuestions.splice(index, 1)
}

const handleSubmit = async () => {
  if (!examFormRef.value) return
  
  await examFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await examApi.updateExam(examForm)
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
  fetchExamDetail()
  searchQuestions()
})
</script>

<style scoped>
.exam-edit-container {
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
