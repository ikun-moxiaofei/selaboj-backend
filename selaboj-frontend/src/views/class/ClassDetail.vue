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
        <h3>班级成员管理</h3>
        
        <div class="member-management">
          <el-form :inline="true" :model="memberForm" class="member-form">
            <el-form-item label="用户ID列表">
              <el-input v-model="memberForm.userIds" placeholder="请输入用户ID，多个用逗号分隔" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleAddMembers">添加成员</el-button>
              <el-button type="danger" @click="handleRemoveMembers">移除成员</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-table :data="memberList" style="width: 100%">
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
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
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

const memberForm = reactive({
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

const handleAddMembers = async () => {
  if (!memberForm.userIds) {
    ElMessage.warning('请输入用户ID')
    return
  }
  
  const userIds = memberForm.userIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
  
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
    memberForm.userIds = ''
  } catch (error) {
    ElMessage.error('添加成员失败')
  }
}

const handleRemoveMembers = async () => {
  if (!memberForm.userIds) {
    ElMessage.warning('请输入用户ID')
    return
  }
  
  const userIds = memberForm.userIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
  
  if (userIds.length === 0) {
    ElMessage.warning('请输入有效的用户ID')
    return
  }
  
  try {
    await classApi.removeClassMembers({
      classId: classId.value,
      userIds: userIds
    })
    ElMessage.success('移除成员成功')
    fetchClassMembers()
    memberForm.userIds = ''
  } catch (error) {
    ElMessage.error('移除成员失败')
  }
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

.class-members h3 {
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: bold;
}

.member-management {
  margin-bottom: 20px;
}

.member-form {
  margin-bottom: 20px;
}
</style>
