<template>
  <div class="user-manage-container">
    <el-card class="user-manage-card">
      <template #header>
        <div class="user-manage-header">
          <h2>用户管理</h2>
        </div>
      </template>
      <el-form :model="searchForm" inline>
        <el-form-item label="账号">
          <el-input v-model="searchForm.userAccount" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button type="success" @click="handleAdd">添加用户</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="userList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userAccount" label="账号" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="userRole" label="角色" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
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
              @change="handleSizeChange"
              class="page-size-select"
            >
              <el-option label="10条/页" :value="10" />
              <el-option label="20条/页" :value="20" />
            </el-select>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>暂无数据</p>
      </div>
    </el-card>

    <!-- 添加用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
        <el-form-item label="账号" prop="userAccount">
          <el-input v-model="userForm.userAccount" />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="userForm.userName" />
        </el-form-item>
        <el-form-item label="角色" prop="userRole">
          <el-select v-model="userForm.userRole">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleDialogSubmit" :loading="loading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import userApi from '../../api/user'

const userList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const userFormRef = ref(null)
const jumpPage = ref('')

const searchForm = reactive({
  userAccount: '',
  userName: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
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

const userForm = reactive({
  id: null,
  userAccount: '',
  userName: '',
  userRole: 'user'
})

const rules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const loadUserList = async () => {
  loading.value = true
  try {
    console.log('请求参数:', {
      current: pagination.current,
      pageSize: pagination.size,
      userAccount: searchForm.userAccount,
      userName: searchForm.userName
    })
    const response = await userApi.getUserList({
      current: pagination.current,
      pageSize: pagination.size,
      userAccount: searchForm.userAccount,
      userName: searchForm.userName
    })
    console.log('响应数据:', response)
    userList.value = response.data?.records || []
    pagination.total = response.data?.total || 0
    console.log('用户列表:', userList.value)
    console.log('总数:', pagination.total)
  } catch (error) {
    console.error('获取用户列表失败', error)
    userList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadUserList()
}

const handleAdd = () => {
  dialogTitle.value = '添加用户'
  userForm.id = null
  userForm.userAccount = ''
  userForm.userName = ''
  userForm.userRole = 'user'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  userForm.id = row.id
  userForm.userAccount = row.userAccount
  userForm.userName = row.userName
  userForm.userRole = row.userRole
  dialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个用户吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await userApi.deleteUser({ id })
      ElMessage.success('删除成功')
      loadUserList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleDialogSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (userForm.id) {
          await userApi.updateUser(userForm)
        } else {
          await userApi.addUser(userForm)
        }
        dialogVisible.value = false
        loadUserList()
      } catch (error) {
        console.error('操作失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadUserList()
}

const handlePrevPage = () => {
  if (pagination.current > 1) {
    pagination.current--
    loadUserList()
  }
}

const handleNextPage = () => {
  if (pagination.current < totalPages.value) {
    pagination.current++
    loadUserList()
  }
}

const handleJumpToPage = (page) => {
  pagination.current = page
  loadUserList()
}

const handleJump = () => {
  const page = parseInt(jumpPage.value)
  if (page && page >= 1 && page <= totalPages.value) {
    pagination.current = page
    jumpPage.value = ''
    loadUserList()
  } else {
    ElMessage.warning('请输入有效的页码')
    jumpPage.value = ''
  }
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
}

.user-manage-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.user-manage-header {
  text-align: center;
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
