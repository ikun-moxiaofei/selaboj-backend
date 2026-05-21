<template>
  <div class="user-manage-container">
    <div class="page-header">
      <h1 class="page-title">学生管理</h1>
      <p class="page-desc">管理班级学生账号</p>
    </div>

    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.userName" 
            placeholder="请输入用户名"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="用户账号">
          <el-input 
            v-model="searchForm.userAccount" 
            placeholder="请输入用户账号"
            class="search-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-section">
      <el-button type="primary" @click="openAddModal">添加学生</el-button>
    </div>

    <div class="table-container">
      <el-table 
        :data="tableData" 
        border 
        :loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="userAccount" label="用户账号" />
        <el-table-column prop="userName" label="学生姓名" />
        <el-table-column prop="userRole" label="角色">
          <template #default="scope">
            <el-tag type="success">学生</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column prop="updateTime" label="更新时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="text" @click="openEditModal(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(scope.row.id)" class="delete-btn">删除</el-button>
          </template>
        </el-table-column>
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

    <!-- 添加弹窗 -->
    <el-dialog title="添加学生" :visible.sync="addModalVisible" width="400px">
      <el-form :model="addForm" class="modal-form">
        <el-form-item label="用户账号" prop="userAccount" :rules="[{ required: true, message: '请输入用户账号' }]">
          <el-input v-model="addForm.userAccount" placeholder="请输入用户账号" />
        </el-form-item>
        <el-form-item label="学生姓名" prop="userName" :rules="[{ required: true, message: '请输入学生姓名' }]">
          <el-input v-model="addForm.userName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="addForm.email" placeholder="请输入邮箱（可选）" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addForm.phone" placeholder="请输入手机号（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addModalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog title="编辑学生" :visible.sync="editModalVisible" width="400px">
      <el-form :model="editForm" class="modal-form">
        <el-form-item label="用户账号">
          <el-input v-model="editForm.userAccount" disabled />
        </el-form-item>
        <el-form-item label="学生姓名" prop="userName" :rules="[{ required: true, message: '请输入学生姓名' }]">
          <el-input v-model="editForm.userName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editModalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import teacherUserApi from '../api/teacherUser'

const loading = ref(false)
const tableData = ref([])
const addModalVisible = ref(false)
const editModalVisible = ref(false)

const searchForm = reactive({
  userName: '',
  userAccount: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const addForm = reactive({
  userAccount: '',
  userName: '',
  email: '',
  phone: ''
})

const editForm = reactive({
  id: null,
  userAccount: '',
  userName: '',
  email: '',
  phone: ''
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await teacherUserApi.listStudents(
      pagination.current,
      pagination.pageSize,
      searchForm.userName,
      searchForm.userAccount
    )
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('获取学生列表失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  searchForm.userName = ''
  searchForm.userAccount = ''
  pagination.current = 1
  fetchUsers()
}

const openAddModal = () => {
  addForm.userAccount = ''
  addForm.userName = ''
  addForm.email = ''
  addForm.phone = ''
  addModalVisible.value = true
}

const openEditModal = async (row) => {
  try {
    const res = await teacherUserApi.getStudent(row.id)
    const user = res.data
    editForm.id = user.id
    editForm.userAccount = user.userAccount
    editForm.userName = user.userName
    editForm.email = user.email || ''
    editForm.phone = user.phone || ''
    editModalVisible.value = true
  } catch (error) {
    ElMessage.error('获取学生信息失败')
  }
}

const handleAdd = async () => {
  if (!addForm.userAccount || !addForm.userName) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    await teacherUserApi.addStudent({
      userAccount: addForm.userAccount,
      userName: addForm.userName,
      email: addForm.email,
      phone: addForm.phone
    })
    ElMessage.success('添加成功')
    addModalVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleEdit = async () => {
  if (!editForm.userName) {
    ElMessage.warning('请填写学生姓名')
    return
  }

  try {
    await teacherUserApi.updateStudent({
      id: editForm.id,
      userName: editForm.userName,
      email: editForm.email,
      phone: editForm.phone
    })
    ElMessage.success('修改成功')
    editModalVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该学生吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await teacherUserApi.deleteStudent(id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.current = 1
  fetchUsers()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-manage-container {
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

.search-input {
  width: 200px;
}

.action-section {
  margin-bottom: 20px;
}

.table-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.delete-btn {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #f78989;
}

.modal-form {
  padding: 20px 0;
}
</style>