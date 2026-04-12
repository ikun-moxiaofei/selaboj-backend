<template>
  <div class="class-add-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>创建班级</h2>
        </div>
      </template>
      
      <el-form :model="classForm" :rules="rules" ref="classFormRef" label-width="100px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="classForm.className" placeholder="请输入班级名称" />
        </el-form-item>
        
        <el-form-item label="班级代码" prop="classCode">
          <el-input v-model="classForm.classCode" placeholder="请输入班级代码" />
        </el-form-item>
        
        <el-form-item label="班级描述" prop="description">
          <el-input
            v-model="classForm.description"
            type="textarea"
            rows="4"
            placeholder="请输入班级描述"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import classApi from '../../api/class'

const router = useRouter()
const classFormRef = ref(null)
const loading = ref(false)

const classForm = reactive({
  className: '',
  classCode: '',
  description: ''
})

const rules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' }
  ],
  classCode: [
    { required: true, message: '请输入班级代码', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入班级描述', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!classFormRef.value) return
  
  await classFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await classApi.addClass(classForm)
        ElMessage.success('创建成功')
        router.back()
      } catch (error) {
        ElMessage.error('创建失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.class-add-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
