<template>
  <div class="question-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>题目列表</h2>
          <el-button type="primary" @click="handleAddQuestion">创建题目</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="题目类型">
          <el-select v-model="searchForm.questionType" placeholder="请选择题目类型">
            <el-option label="全部" value="" />
            <el-option label="编程题" value="0" />
            <el-option label="选择题" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input v-model="searchForm.keyword" placeholder="请输入题目名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="questionList" style="width: 100%" :loading="loading">
        <el-table-column prop="id" label="题目ID" width="80" />
        <el-table-column prop="title" label="题目名称" min-width="200">
          <template #default="scope">
            <el-button type="text" @click="handleViewQuestion(scope.row.id)">{{ scope.row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="questionType" label="题目类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.questionType === 0" size="small">编程题</el-tag>
            <el-tag v-else type="success" size="small">选择题</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitNum" label="提交数" width="100" />
        <el-table-column prop="passNum" label="通过数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditQuestion(scope.row.id)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteQuestion(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="pagination.total > 0" class="pagination-container">
        <span class="pagination-info">共 {{ pagination.total }} 条记录</span>
        <div class="pagination-wrapper">
          <div class="custom-pagination">
            <el-button 
              type="text" 
              @click="handlePrevPage" 
              :disabled="pagination.current <= 1"
              class="pagination-btn"
            >上一页</el-button>
            
            <el-button 
              v-for="page in visiblePages" 
              :key="page"
              @click="handleJumpToPage(page)"
              :class="['pagination-btn', { 'active': page === pagination.current }]"
            >{{ page }}</el-button>
            
            <span v-if="showPrevEllipsis" class="ellipsis">...</span>
            <span v-if="showNextEllipsis" class="ellipsis">...</span>
            
            <el-button 
              type="text" 
              @click="handleNextPage" 
              :disabled="pagination.current >= totalPages"
              class="pagination-btn"
            >下一页</el-button>
            
            <div class="page-jumper">
              <el-input 
                v-model="jumpPage" 
                type="number" 
                min="1" 
                :max="totalPages"
                class="jump-input"
                placeholder="页码"
              />
              <el-button type="primary" @click="handleJump">跳转</el-button>
            </div>
            
            <el-select 
              v-model="pagination.size" 
              @change="handleSizeChange(pagination.size)"
              class="page-size-select"
            >
              <el-option label="10条/页" :value="10" />
              <el-option label="20条/页" :value="20" />
              <el-option label="50条/页" :value="50" />
            </el-select>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>暂无数据</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import questionApi from '../../api/question'

const router = useRouter()
const questionList = ref([])
const loading = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})

const jumpPage = ref('')

const searchForm = reactive({
  questionType: '',
  keyword: ''
})

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(pagination.total / pagination.size))
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = pagination.current
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
    } else if (current >= total - 3) {
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      for (let i = current - 2; i <= current + 2; i++) {
        pages.push(i)
      }
    }
  }
  return pages
})

const showPrevEllipsis = computed(() => {
  return totalPages.value > 7 && pagination.current > 4
})

const showNextEllipsis = computed(() => {
  return totalPages.value > 7 && pagination.current < totalPages.value - 3
})

const handlePrevPage = () => {
  if (pagination.current > 1) {
    pagination.current--
    fetchQuestionList()
  }
}

const handleNextPage = () => {
  if (pagination.current < totalPages.value) {
    pagination.current++
    fetchQuestionList()
  }
}

const handleJumpToPage = (page) => {
  pagination.current = page
  fetchQuestionList()
}

const handleJump = () => {
  const page = parseInt(jumpPage.value)
  if (page && page >= 1 && page <= totalPages.value) {
    pagination.current = page
    jumpPage.value = ''
    fetchQuestionList()
  } else {
    ElMessage.warning('请输入有效的页码')
    jumpPage.value = ''
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchQuestionList()
}

const resetForm = () => {
  searchForm.questionType = ''
  searchForm.keyword = ''
  pagination.current = 1
  fetchQuestionList()
}

const fetchQuestionList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.size
    }
    if (searchForm.questionType !== '') {
      params.questionType = parseInt(searchForm.questionType)
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const response = await questionApi.listQuestionVOByPage(params)
    if (response && response.data) {
      questionList.value = response.data.records || []
      pagination.total = response.data.total || 0
      pagination.pages = response.data.pages || 0
      console.log('分页数据:', response.data)
    } else {
      questionList.value = []
      pagination.total = 0
      pagination.pages = 0
    }
  } catch (error) {
    console.error('获取题目列表失败', error)
    questionList.value = []
    pagination.total = 0
    pagination.pages = 0
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}

const handleAddQuestion = () => {
  router.push('/question/add')
}

const handleViewQuestion = (id) => {
  router.push(`/question/detail/${id}`)
}

const handleEditQuestion = (id) => {
  router.push(`/question/edit/${id}`)
}

const handleDeleteQuestion = (id) => {
  ElMessageBox.confirm('确定要删除这个题目吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      console.log('删除题目ID:', id)
      const response = await questionApi.deleteQuestion({ id })
      console.log('删除响应:', response)
      if (response && response.data === true) {
        ElMessage.success('删除成功')
        fetchQuestionList()
      } else {
        ElMessage.error('删除失败：' + (response?.message || '未知错误'))
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败：' + (error?.message || '网络错误'))
    }
  }).catch(() => {})
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchQuestionList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  fetchQuestionList()
}

onMounted(() => {
  fetchQuestionList()
})
</script>

<style scoped>
.question-list-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-top: 1px solid #eee;
}

.pagination-wrapper {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.custom-pagination {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  border: 1px solid #dcdfe6;
  background: #fff;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  color: #409eff;
  border-color: #c6e2ff;
  background: #ecf5ff;
}

.pagination-btn.active {
  color: #fff;
  background: #409eff;
  border-color: #409eff;
}

.pagination-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.ellipsis {
  padding: 0 8px;
  color: #909399;
  font-size: 14px;
}

.page-jumper {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
}

.jump-input {
  width: 60px;
  height: 32px;
}

.page-size-select {
  margin-left: 8px;
  width: 100px;
}

.pagination-info {
  font-size: 14px;
  color: #606266;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>