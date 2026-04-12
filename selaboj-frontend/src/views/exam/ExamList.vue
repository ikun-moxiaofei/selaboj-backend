<template>
  <div class="exam-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>考试列表</h2>
          <el-button type="primary" @click="handleAddExam">创建考试</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input v-model="searchForm.keyword" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="examList" style="width: 100%">
        <el-table-column prop="id" label="考试ID" width="80" />
        <el-table-column prop="examName" label="考试名称" min-width="200">
          <template #default="scope">
            <el-button type="text" @click="handleViewExam(scope.row.id)">{{ scope.row.examName }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditExam(scope.row.id)">编辑</el-button>
            <el-button type="success" size="small" @click="handlePublishExam(scope.row.id)">发布</el-button>
            <el-button type="danger" size="small" @click="handleDeleteExam(scope.row.id)">删除</el-button>
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
import examApi from '../../api/exam'

const router = useRouter()
const examList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: ''
})

const handleSearch = () => {
  currentPage.value = 1
  fetchExamList()
}

const resetForm = () => {
  searchForm.keyword = ''
  currentPage.value = 1
  fetchExamList()
}

const fetchExamList = async () => {
  try {
    const response = await examApi.listExamVOByPage({
      current: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword
    })
    examList.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    console.error('获取考试列表失败', error)
  }
}

const handleAddExam = () => {
  router.push('/exam/add')
}

const handleViewExam = (id) => {
  router.push(`/exam/detail/${id}`)
}

const handleEditExam = (id) => {
  router.push(`/exam/edit/${id}`)
}

const handlePublishExam = (id) => {
  // 弹出发布对话框
  ElMessageBox.prompt('请输入班级ID', '发布考试', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^\d+$/,
    inputErrorMessage: '请输入有效的班级ID'
  }).then(async ({ value }) => {
    try {
      await examApi.publishExamToClass(id, value)
      ElMessage.success('发布成功')
    } catch (error) {
      ElMessage.error('发布失败')
    }
  }).catch(() => {})
}

const handleDeleteExam = (id) => {
  ElMessageBox.confirm('确定要删除这个考试吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await examApi.deleteExam({ id })
      ElMessage.success('删除成功')
      fetchExamList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchExamList()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchExamList()
}

onMounted(() => {
  fetchExamList()
})
</script>

<style scoped>
.exam-list-container {
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
