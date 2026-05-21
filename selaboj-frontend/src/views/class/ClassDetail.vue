<template>
  <div class="class-detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ classInfo.className }}</h2>
          <el-button type="primary" @click="$router.push(`/class/edit/${classInfo.id}`)">编辑班级</el-button>
        </div>
      </template>
      
      <div class="class-info">
        <el-descriptions :column="2">
          <el-descriptions-item label="班级ID">{{ classInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="班级代码">{{ classInfo.classCode }}</el-descriptions-item>
          <el-descriptions-item label="班级描述" :span="2">{{ classInfo.description }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="class-members">
        <div class="members-header">
          <h3>班级成员管理</h3>
          <div class="members-actions">
            <el-button type="primary" @click="showAddDialog = true">添加成员</el-button>
            <el-button type="danger" @click="handleBatchRemove" :disabled="selectedMemberIds.length === 0">
              移除选中 ({{ selectedMemberIds.length }})
            </el-button>
          </div>
        </div>
        
        <el-table :data="memberList" style="width: 100%" :row-key="(row) => row.id" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="用户ID" width="80" />
          <el-table-column prop="userName" label="用户姓名" width="120" />
          <el-table-column prop="userAccount" label="用户账号" width="150" />
          <el-table-column prop="userRole" label="用户角色" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.userRole === 'user'" size="small">学生</el-tag>
              <el-tag v-else-if="scope.row.userRole === 'teacher'" type="success" size="small">老师</el-tag>
              <el-tag v-else type="warning" size="small">管理员</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button type="danger" size="small" @click="handleRemoveSingle(scope.row.id)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 添加成员对话框 -->
    <el-dialog v-model="showAddDialog" title="添加成员" width="450px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="用户ID列表">
          <el-input v-model="addForm.userIds" placeholder="请输入用户ID，多个用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddMembers">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import classApi from '../../api/class'

const route = useRoute()
const classId = ref(route.params.id)
const classInfo = ref({
  id: '',
  className: '',
  classCode: '',
  description: ''
})
const memberList = ref([])
const selectedMemberIds = ref([])
const showAddDialog = ref(false)

const addForm = reactive({
  userIds: ''
})

const fetchClassDetail = async () => {
  try {
    const response = await classApi.getClassVO(classId.value)
    classInfo.value = response.data
  } catch (error) {
    console.error('获取班级详情失败', error)
  }
}

const fetchClassMembers = async () => {
  try {
    const response = await classApi.getClassMembers(classId.value)
    memberList.value = response.data
  } catch (error) {
    console.error('获取班级成员失败', error)
  }
}

const handleSelectionChange = (val) => {
  selectedMemberIds.value = val.map(item => item.id)
}

const handleAddMembers = async () => {
  if (!addForm.userIds) {
    ElMessage.warning('请输入用户ID')
    return
  }
  
  const userIds = addForm.userIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
  
  if (userIds.length === 0) {
    ElMessage.warning('请输入有效的用户ID')
    return
  }
  
  try {
    await classApi.addClassMembers({
      classId: classId.value,
      userIds: userIds
    })
    ElMessage.success('添加成员成功')
    fetchClassMembers()
    showAddDialog.value = false
    addForm.userIds = ''
  } catch (error) {
    ElMessage.error('添加成员失败')
  }
}

const handleRemoveSingle = (userId) => {
  ElMessageBox.confirm('确定要移除该成员吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await classApi.removeClassMembers({
        classId: classId.value,
        userIds: [userId]
      })
      ElMessage.success('移除成功')
      fetchClassMembers()
    } catch (error) {
      ElMessage.error('移除失败')
    }
  }).catch(() => {})
}

const handleBatchRemove = () => {
  if (selectedMemberIds.value.length === 0) {
    ElMessage.warning('请先选择要移除的成员')
    return
  }
  
  ElMessageBox.confirm(`确定要移除选中的 ${selectedMemberIds.value.length} 位成员吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await classApi.removeClassMembers({
        classId: classId.value,
        userIds: selectedMemberIds.value
      })
      ElMessage.success('移除成功')
      fetchClassMembers()
      selectedMemberIds.value = []
    } catch (error) {
      ElMessage.error('移除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchClassDetail()
  fetchClassMembers()
})
</script>

<style scoped>
.class-detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.class-info {
  margin: 20px 0;
}

.class-members {
  margin-top: 30px;
}

.members-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.members-header h3 {
  font-size: 16px;
  font-weight: bold;
  margin: 0;
}

.members-actions {
  display: flex;
  gap: 10px;
}

.member-management {
  margin-bottom: 20px;
}

.member-form {
  margin-bottom: 20px;
}
</style>
