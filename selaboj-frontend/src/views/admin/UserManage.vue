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
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="userRole" label="角色" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
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
import { ref, reactive, onMounted } from 'vue'
import userApi from '../../api/user'

const userList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const userFormRef = ref(null)

const searchForm = reactive({
  userAccount: '',
  userName: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const userForm = reactive({
  id: null,
  userAccount: '',
  userName: '',
  email: '',
  userRole: 'user'
})

const rules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const loadUserList = async () => {
  loading.value = true
  try {
    const response = await userApi.getUserList({
      current: pagination.current,
      pageSize: pagination.size,
      userAccount: searchForm.userAccount,
      userName: searchForm.userName
    })
    userList.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    console.error('获取用户列表失败', error)
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
  userForm.email = ''
  userForm.userRole = 'user'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  userForm.id = row.id
  userForm.userAccount = row.userAccount
  userForm.userName = row.userName
  userForm.email = row.email
  userForm.userRole = row.userRole
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await userApi.deleteUser({ id })
    loadUserList()
  } catch (error) {
    console.error('删除用户失败', error)
  }
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
  loadUserList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadUserList()
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
  text-align: right;
}
</style>
