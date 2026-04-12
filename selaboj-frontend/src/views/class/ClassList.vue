<template>
  <div class="class-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>班级列表</h2>
          <div>
            <el-button v-if="userStore.userRole === 'teacher' || userStore.userRole === 'admin'" type="primary" @click="handleAddClass">创建班级</el-button>
            <el-button v-if="userStore.userRole === 'user'" type="success" @click="showJoinDialog">加入班级</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input v-model="searchForm.keyword" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="classList" style="width: 100%">
        <el-table-column prop="id" label="班级ID" width="80" />
        <el-table-column prop="className" label="班级名称" min-width="200">
          <template #default="scope">
            <el-button type="text" @click="handleViewClass(scope.row.id)">{{ scope.row.className }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="classCode" label="班级代码" width="150" />
        <el-table-column prop="description" label="班级描述" min-width="200" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button v-if="userStore.userRole === 'teacher' || userStore.userRole === 'admin'" type="primary" size="small" @click="handleEditClass(scope.row.id)">编辑</el-button>
            <el-button v-if="userStore.userRole === 'teacher' || userStore.userRole === 'admin'" type="danger" size="small" @click="handleDeleteClass(scope.row.id)">删除</el-button>
            <el-button v-if="userStore.userRole === 'user'" type="warning" size="small" @click="handleLeaveClass(scope.row.id)">退出</el-button>
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
    
    <!-- 加入班级对话框 -->
    <el-dialog v-model="joinDialogVisible" title="加入班级" width="400px">
      <el-form :model="joinForm" label-width="100px">
        <el-form-item label="班级代码">
          <el-input v-model="joinForm.classCode" placeholder="请输入班级代码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="joinDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleJoinClass">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import classApi from '../../api/class'

const router = useRouter()
const userStore = useUserStore()
const classList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: ''
})

const joinDialogVisible = ref(false)
const joinForm = reactive({
  classCode: ''
})

const handleSearch = () => {
  currentPage.value = 1
  fetchClassList()
}

const resetForm = () => {
  searchForm.keyword = ''
  currentPage.value = 1
  fetchClassList()
}

const fetchClassList = async () => {
  try {
    // 根据用户角色获取不同的班级列表
    if (userStore.userRole === 'user') {
      // 学生获取自己加入的班级
      const response = await classApi.getMyClasses()
      classList.value = response.data || []
      total.value = classList.value.length
    } else {
      // 老师和管理员获取所有班级
      const response = await classApi.listClassVOByPage({
        current: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchForm.keyword
      })
      classList.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取班级列表失败', error)
  }
}

const handleAddClass = () => {
  router.push('/class/add')
}

const handleViewClass = (id) => {
  router.push(`/class/detail/${id}`)
}

const handleEditClass = (id) => {
  router.push(`/class/edit/${id}`)
}

const handleDeleteClass = (id) => {
  ElMessageBox.confirm('确定要删除这个班级吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await classApi.deleteClass({ id })
      ElMessage.success('删除成功')
      fetchClassList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const showJoinDialog = () => {
  joinDialogVisible.value = true
  joinForm.classCode = ''
}

const handleJoinClass = async () => {
  if (!joinForm.classCode || joinForm.classCode.trim() === '') {
    ElMessage.warning('请输入班级代码')
    return
  }
  
  try {
    await classApi.joinClass(joinForm.classCode)
    ElMessage.success('加入班级成功')
    joinDialogVisible.value = false
    fetchClassList()
  } catch (error) {
    ElMessage.error('加入班级失败：' + (error.message || '班级代码错误'))
  }
}

const handleLeaveClass = (classId) => {
  ElMessageBox.confirm('确定要退出这个班级吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await classApi.leaveClass(classId)
      ElMessage.success('退出班级成功')
      fetchClassList()
    } catch (error) {
      ElMessage.error('退出班级失败')
    }
  }).catch(() => {})
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchClassList()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchClassList()
}

onMounted(() => {
  fetchClassList()
})
</script>

<style scoped>
.class-list-container {
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
