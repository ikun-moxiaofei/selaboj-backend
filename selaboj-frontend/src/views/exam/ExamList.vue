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
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button v-if="userRole === 'teacher' || userRole === 'admin'" type="primary" size="small" @click="handleEditExam(scope.row.id)">编辑</el-button>
            <el-button v-if="userRole === 'teacher' || userRole === 'admin'" type="success" size="small" @click="handlePublishExam(scope.row.id)">发布</el-button>
            <el-button v-if="userRole === 'teacher' || userRole === 'admin'" type="danger" size="small" @click="handleDeleteExam(scope.row.id)">删除</el-button>
            <el-button v-if="userRole === 'user' || userRole === 'student'" type="info" size="small" @click="handleStartExam(scope.row.id)">开始考试</el-button>
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

    <!-- 发布考试对话框 -->
    <el-dialog title="发布考试" v-model="publishDialogVisible" width="400px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="选择班级" prop="classId">
          <el-select v-model="publishForm.classId" placeholder="请选择班级">
            <el-option 
              v-for="clazz in classList" 
              :key="clazz.id" 
              :label="clazz.className" 
              :value="clazz.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <span class="tips">发布后，该班级的所有学生将获得考试资格</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPublish">确定发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import examApi from '../../api/exam'
import classApi from '../../api/class'

const router = useRouter()
const userStore = useUserStore()
const examList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: ''
})

// 发布考试相关
const publishDialogVisible = ref(false)
const publishForm = reactive({
  examId: null,
  classId: null
})
const classList = ref([])
const userRole = ref('')

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
    const role = userStore.user?.userRole || ''
    userRole.value = role
    
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

const handleStartExam = async (id) => {
  try {
    const response = await examApi.startExam(id)
    ElMessage.success('开始考试成功')
    router.push(`/exam/detail/${id}?recordId=${response.data}`)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '开始考试失败')
  }
}

const handlePublishExam = async (id) => {
  console.log('handlePublishExam called with id:', id)
  publishForm.examId = id
  publishForm.classId = null
  try {
    await fetchClassList()
    console.log('classList:', classList.value)
    publishDialogVisible.value = true
    console.log('publishDialogVisible set to:', publishDialogVisible.value)
  } catch (error) {
    console.error('Error in handlePublishExam:', error)
    ElMessage.error('获取班级列表失败')
  }
}

const fetchClassList = async () => {
  console.log('fetchClassList called')
  try {
    console.log('Sending request to classApi.listClassVOByPage with params:', { current: 1, pageSize: 20 })
    const response = await classApi.listClassVOByPage({
      current: 1,
      pageSize: 20
    })
    console.log('Response received:', response)
    classList.value = response.data?.records || []
    console.log('班级列表数据:', classList.value)
  } catch (error) {
    console.error('获取班级列表失败:', error)
    console.error('Error response:', error.response)
    classList.value = []
  }
}

const confirmPublish = async () => {
  if (!publishForm.classId) {
    ElMessage.warning('请选择班级')
    return
  }
  
  try {
    await examApi.publishExamToClass(publishForm.examId, publishForm.classId)
    ElMessage.success('发布成功')
    publishDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发布失败')
  }
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

onMounted(async () => {
  userStore.initUser()
  await fetchExamList()
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

.tips {
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style>
