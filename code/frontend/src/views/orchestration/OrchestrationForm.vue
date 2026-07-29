<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { orchestrationApi, type Orchestration } from '@/api/orchestration'

const route = useRoute()
const router = useRouter()
const orchestrationId = route.params.id as string

const isEdit = computed(() => !!orchestrationId)
const loading = ref(false)
const form = ref<Orchestration>({
  name: '',
  code: '',
  description: '',
  status: 'DRAFT'
})

const loadData = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await orchestrationApi.getById(orchestrationId)
    form.value = {
      name: data.name,
      code: data.code,
      description: data.description,
      status: data.status
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入编排名称')
    return
  }
  try {
    if (isEdit.value) {
      await orchestrationApi.update(orchestrationId, {
        name: form.value.name,
        description: form.value.description,
        status: form.value.status
      })
      ElMessage.success('更新成功')
      router.push(`/orchestrations/${orchestrationId}`)
    } else {
      const res = await orchestrationApi.create({
        name: form.value.name!,
        code: form.value.code || undefined,
        description: form.value.description || undefined
      })
      ElMessage.success('创建成功')
      if (res?.id) {
        router.push(`/orchestrations/${res.id}`)
      } else {
        router.push('/orchestrations')
      }
    }
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const handleCancel = () => {
  if (isEdit.value) {
    router.push(`/orchestrations/${orchestrationId}`)
  } else {
    router.push('/orchestrations')
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="orchestration-form" v-loading="loading">
    <div class="form-header">
      <el-button :icon="ArrowLeft" @click="handleCancel">返回</el-button>
      <h2>{{ isEdit ? '编辑业务编排' : '新建业务编排' }}</h2>
    </div>

    <div class="form-container">
      <el-form
        :model="form"
        label-width="100px"
        class="orchestration-form-inner"
      >
        <el-form-item label="编排名称" required>
          <el-input v-model="form.name" placeholder="请输入编排名称" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="编排编码" v-if="!isEdit">
          <el-input v-model="form.code" placeholder="留空自动生成，如 ORCH-0001" />
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
            placeholder="请输入编排描述，说明该业务编排的用途和目标"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.orchestration-form {
  .form-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;

    h2 {
      color: var(--primary-color);
      margin: 0;
    }
  }

  .form-container {
    background: white;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    max-width: 700px;
  }

  .orchestration-form-inner {
    .el-form-item {
      margin-bottom: 20px;
    }
  }
}
</style>
