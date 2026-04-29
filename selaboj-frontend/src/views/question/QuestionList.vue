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
      
      <el-table :data="questionList" style="width: 100%">
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
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import questionApi from '../../api/question'

const router = useRouter()
const questionList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  questionType: '',
  keyword: ''
})

const handleSearch = () => {
  currentPage.value = 1
  fetchQuestionList()
}

const resetForm = () => {
  searchForm.questionType = ''
  searchForm.keyword = ''
  currentPage.value = 1
  fetchQuestionList()
}

const fetchQuestionList = async () => {
  try {
    const params = {
      current: currentPage.value,
      pageSize: pageSize.value
    }
    if (searchForm.questionType !== '') {
      params.questionType = parseInt(searchForm.questionType)
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const response = await questionApi.listQuestionVOByPage(params)
    questionList.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    console.error('获取题目列表失败', error)
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
      await questionApi.deleteQuestion({ id })
      ElMessage.success('删除成功')
      fetchQuestionList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchQuestionList()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
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
  justify-content: flex-end;
}
</style>
