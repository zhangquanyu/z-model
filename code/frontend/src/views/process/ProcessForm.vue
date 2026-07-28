<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { processApi } from '@/api/process'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const isDesign = computed(() => route.path.includes('/design'))
const processId = computed(() => route.params.id as string)

const form = ref({
  name: '',
  code: '',
  description: '',
  status: 'DRAFT'
})

const loading = ref(false)
const saving = ref(false)

const loadData = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await processApi.getById(processId.value)
    form.value = {
      name: res.name,
      code: res.code,
      description: res.description || '',
      status: (res.status as string) || 'DRAFT'
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入流程名称')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await processApi.update(processId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      const res = await processApi.create(form.value)
      ElMessage.success('创建成功')
      if (isDesign.value && isEdit.value) {
        router.push(`/processes/${res.id}/design`)
      } else {
        router.push('/processes')
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  router.push('/processes')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="process-form" v-loading="loading">
    <h2 class="form-title">{{ isEdit ? '编辑流程' : '新建流程' }}</h2>
    
    <el-form :model="form" label-width="100px" class="form-container">
      <el-form-item label="流程名称" required>
        <el-input v-model="form.name" placeholder="请输入流程名称" maxlength="100" />
      </el-form-item>
      
      <el-form-item label="流程编码">
        <el-input v-model="form.code" placeholder="留空自动生成，如 PROC-0001" maxlength="50" :disabled="isEdit" />
      </el-form-item>
      
      <el-form-item label="状态" v-if="isEdit">
        <el-select v-model="form.status" style="width: 200px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已启用" value="ACTIVE" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入流程描述"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style lang="scss" scoped>
.process-form {
  max-width: 800px;
  
  .form-title {
    margin-bottom: 24px;
    color: var(--primary-color);
  }
  
  .form-container {
    background: white;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}
</style>
