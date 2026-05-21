<template>
  <div class="grade-container">
    <div class="page-header">
      <h1 class="page-title">成绩查询</h1>
      <p class="page-desc">查看学生考试成绩</p>
    </div>

    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="查询方式">
          <el-radio-group v-model="searchForm.queryType" @change="handleQueryTypeChange">
            <el-radio label="exam">按考试查询</el-radio>
            <el-radio label="class">按班级查询</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="searchForm.queryType === 'exam'" label="选择考试">
          <el-select 
            v-model="searchForm.examId" 
            placeholder="请选择考试"
            class="search-select"
            @change="handleExamChange"
          >
            <el-option label="全部考试" :value="0" />
            <el-option 
              v-for="exam in examList" 
              :key="exam.id" 
              :label="exam.examName" 
              :value="exam.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="searchForm.queryType === 'class'" label="选择班级">
          <el-select 
            v-model="searchForm.classId" 
            placeholder="请选择班级"
            class="search-select"
            @change="handleClassChange"
          >
            <el-option 
              v-for="clazz in classList" 
              :key="clazz.id" 
              :label="clazz.className" 
              :value="clazz.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchGrades">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="stats-card">
      <div class="stat-item">
        <span class="stat-label">参考人数</span>
        <span class="stat-value">{{ stats.participantCount }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">平均分</span>
        <span class="stat-value">{{ stats.averageScore }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">最高分</span>
        <span class="stat-value">{{ stats.highestScore }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">最低分</span>
        <span class="stat-value">{{ stats.lowestScore }}</span>
      </div>
    </div>

    <div class="table-container">
      <el-table 
        :data="tableData" 
        border 
        :loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="userName" label="学生姓名" />
        <el-table-column prop="examName" label="考试名称" />
        <el-table-column prop="totalScore" label="总分">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.totalScore)">{{ scope.row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
      </el-table>

      <el-pagination
        :current-page="pagination.current"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import gradeApi from '../api/grade'
import classApi from '../api/class'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const examList = ref([])
const classList = ref([])
const tableData = ref([])

const searchForm = reactive({
  queryType: 'exam',
  examId: 0,
  classId: null
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const stats = reactive({
  participantCount: 0,
  averageScore: 0,
  highestScore: 0,
  lowestScore: 0
})

const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 70) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const fetchExamList = async () => {
  try {
    const res = await gradeApi.getExamList()
    examList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  }
}

const fetchClassList = async () => {
  try {
    const res = await classApi.listClassVOByPage({
      current: 1,
      pageSize: 20
    })
    classList.value = res.data?.records || []
  } catch (error) {
    ElMessage.error('获取班级列表失败')
  }
}

const fetchGrades = async () => {
  loading.value = true
  try {
    let res
    if (searchForm.queryType === 'exam') {
      const examId = searchForm.examId === 0 ? '' : searchForm.examId
      res = await gradeApi.getExamGrades(examId, pagination.current, pagination.pageSize)
    } else {
      if (!searchForm.classId) {
        ElMessage.warning('请选择班级')
        loading.value = false
        return
      }
      res = await gradeApi.getClassGrades(searchForm.classId, pagination.current, pagination.pageSize)
    }
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    calculateStats()
  } catch (error) {
    ElMessage.error('查询成绩失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  if (tableData.value.length === 0) {
    stats.participantCount = 0
    stats.averageScore = 0
    stats.highestScore = 0
    stats.lowestScore = 0
    return
  }

  const completedRecords = tableData.value.filter(item => item.status === 2)
  if (completedRecords.length === 0) {
    stats.participantCount = tableData.value.length
    stats.averageScore = 0
    stats.highestScore = 0
    stats.lowestScore = 0
    return
  }

  const scores = completedRecords.map(item => item.totalScore)
  stats.participantCount = completedRecords.length
  stats.averageScore = (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
  stats.highestScore = Math.max(...scores)
  stats.lowestScore = Math.min(...scores)
}

const handleQueryTypeChange = () => {
  pagination.current = 1
  tableData.value = []
  pagination.total = 0
  if (searchForm.queryType === 'class') {
    fetchClassList()
  }
}

const handleExamChange = () => {
  pagination.current = 1
  fetchGrades()
}

const handleClassChange = () => {
  pagination.current = 1
  fetchGrades()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.current = 1
  fetchGrades()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  fetchGrades()
}

onMounted(() => {
  fetchExamList()
})
</script>

<style scoped>
.grade-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-desc {
  color: #909399;
  margin: 0;
}

.search-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.search-form {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-select {
  width: 240px;
}

.stats-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 40px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.table-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-pass {
  color: #e6a23c;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

.view-btn {
  color: #409eff;
}

.view-btn:hover {
  color: #67c23a;
}

.detail-content {
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.detail-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.detail-info .label {
  color: #909399;
}

.detail-info .score {
  font-size: 20px;
  font-weight: bold;
  color: #67c23a;
}

.detail-body h3 {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 16px 0;
  color: #303133;
}

.answer-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.answer-item {
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid;
}

.answer-correct {
  background: #f0f9eb;
  border-color: #67c23a;
}

.answer-wrong {
  background: #fef0f0;
  border-color: #f56c6c;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-num {
  font-weight: bold;
  color: #303133;
}

.correct-tag {
  background: #e8f5e9;
  color: #67c23a;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.wrong-tag {
  background: #fee2e2;
  color: #f56c6c;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.answer-content {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 14px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px;
}
</style>