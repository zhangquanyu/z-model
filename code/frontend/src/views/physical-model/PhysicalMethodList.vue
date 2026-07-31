<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { physicalMethodApi, type PhysicalMethod } from '@/api/physical-model'
import { methodApi } from '@/api/method'
import { ArrowLeft, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const physicalModelId = route.params.id as string

const methods = ref<PhysicalMethod[]>([])
const sourceMethods = ref<any[]>([])
const showCreateDialog = ref(false)
const editingMethod = ref<PhysicalMethod | null>(null)
const creatingMethod = ref<PhysicalMethod>({
  physicalModelId: physicalModelId,
  sourceMethodId: '',
  name: '',
  code: '',
  methodType: 'SELECT',
  sqlTemplate: ''
})

const loadSourceMethods = async () => {
  try {
    const res = await methodApi.list({})
    sourceMethods.value = res.content || []
  } catch (error) {
    console.error('Failed to load source methods:', error)
  }
}

const loadMethods = async () => {
  try {
    methods.value = await physicalMethodApi.listByPhysicalModelId(physicalModelId)
  } catch (error) {
    console.error('Failed to load physical methods:', error)
  }
}

const handleGoBack = () => {
  router.push(`/physical-models/${physicalModelId}`)
}

const handleCreate = () => {
  creatingMethod.value = {
    physicalModelId: physicalModelId,
    sourceMethodId: '',
    name: '',
    code: '',
    methodType: 'SELECT',
    sqlTemplate: ''
  }
  editingMethod.value = null
  showCreateDialog.value = true
}

const handleEdit = (method: PhysicalMethod) => {
  editingMethod.value = { ...method }
  creatingMethod.value = { ...method }
  showCreateDialog.value = true
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个方法吗？', '提示', {
      type: 'warning'
    })
    await physicalMethodApi.delete(id)
    ElMessage.success('删除成功')
    loadMethods()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete method:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    if (editingMethod.value?.id) {
      await physicalMethodApi.update(editingMethod.value.id, creatingMethod.value)
      ElMessage.success('更新成功')
    } else {
      await physicalMethodApi.create(creatingMethod.value)
      ElMessage.success('创建成功')
    }
    showCreateDialog.value = false
    loadMethods()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
    console.error('Failed to save method:', error)
  }
}

const handleSourceMethodChange = (sourceMethodId: string) => {
  const source = sourceMethods.value.find(m => m.id === sourceMethodId)
  if (source && !creatingMethod.value.name) {
    creatingMethod.value.name = source.name
    creatingMethod.value.description = source.description
  }
}

const methodTypes = ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'CUSTOM']

onMounted(() => {
  loadSourceMethods()
  loadMethods()
})
</script>

<template>
  <div class="physical-methods-page">
    <div class="page-header">
      <button class="back-btn" @click="handleGoBack">
        <ArrowLeft />
        返回详情
      </button>
      <h2 class="page-title">扩展方法管理</h2>
      <button class="create-btn" @click="handleCreate">
        <Plus />
        新建方法
      </button>
    </div>

    <div class="methods-container">
      <div class="methods-list" v-if="methods.length > 0">
        <div class="method-card" v-for="method in methods" :key="method.id">
          <div class="method-header">
            <div class="method-info">
              <span class="method-name">{{ method.name }}</span>
              <span :class="['method-type', method.methodType?.toLowerCase()]">
                {{ method.methodType }}
              </span>
            </div>
            <div class="method-actions">
              <button class="action-btn edit" @click="handleEdit(method)">
                <Edit />
              </button>
              <button class="action-btn delete" @click="handleDelete(method.id!)">
                <Delete />
              </button>
            </div>
          </div>
          <div class="method-body">
            <div class="field">
              <label>源方法</label>
              <span class="value" v-if="method.sourceMethodName">{{ method.sourceMethodName }}</span>
              <span class="value" v-else>-</span>
            </div>
            <div class="field" v-if="method.code">
              <label>编号</label>
              <span class="value">{{ method.code }}</span>
            </div>
          </div>
          <div class="method-sql" v-if="method.sqlTemplate">
            <label>SQL 模板</label>
            <pre>{{ method.sqlTemplate }}</pre>
          </div>
          <div class="method-description" v-if="method.description">
            {{ method.description }}
          </div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <p>暂无扩展方法</p>
        <button class="create-btn" @click="handleCreate">
          <Plus />
          添加第一个方法
        </button>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="showCreateDialog" :title="editingMethod ? '编辑方法' : '新建方法'" width="600px">
      <el-form :model="creatingMethod" label-width="120px">
        <el-form-item label="源方法" required>
          <el-select 
            v-model="creatingMethod.sourceMethodId" 
            placeholder="选择源方法"
            style="width: 100%"
            @change="handleSourceMethodChange"
          >
            <el-option v-for="sm in sourceMethods" :key="sm.id" :label="sm.name" :value="sm.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="方法名称">
          <el-input v-model="creatingMethod.name" placeholder="留空则使用源方法名称" />
        </el-form-item>
        <el-form-item label="方法编号">
          <el-input v-model="creatingMethod.code" placeholder="请输入编号（可选）" />
        </el-form-item>
        <el-form-item label="方法类型" required>
          <el-select v-model="creatingMethod.methodType" style="width: 100%">
            <el-option v-for="mt in methodTypes" :key="mt" :label="mt" :value="mt" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="creatingMethod.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="SQL 模板">
          <el-input 
            v-model="creatingMethod.sqlTemplate" 
            type="textarea" 
            :rows="4" 
            placeholder="自定义 SQL 模板，留空则自动生成"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.physical-methods-page {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background-color: #f5f7fa;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #e8ebf0;
  }

  svg {
    width: 16px;
    height: 16px;
  }
}

.page-title {
  flex: 1;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: #1e3a5f;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #2d4a6f;
  }

  svg {
    width: 16px;
    height: 16px;
  }
}

.methods-container {
  min-height: 400px;
}

.methods-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

.method-card {
  background-color: #fafbfc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.method-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.method-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.method-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.method-type {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.select {
    background-color: #e3f2fd;
    color: #2196f3;
  }

  &.insert {
    background-color: #e8f5e9;
    color: #4caf50;
  }

  &.update {
    background-color: #fff3e0;
    color: #ff9800;
  }

  &.delete {
    background-color: #ffebee;
    color: #f44336;
  }

  &.custom {
    background-color: #f3e5f5;
    color: #9c27b0;
  }
}

.method-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;

  svg {
    width: 14px;
    height: 14px;
  }

  &.edit {
    background-color: #fff3e0;

    svg {
      color: #ff9800;
    }

    &:hover {
      background-color: #ffe0b2;
    }
  }

  &.delete {
    background-color: #ffebee;

    svg {
      color: #f44336;
    }

    &:hover {
      background-color: #ffcdd2;
    }
  }
}

.method-body {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 2px;

  label {
    font-size: 12px;
    color: #999;
  }

  .value {
    font-size: 13px;
    color: #333;
  }
}

.method-sql {
  background-color: #f5f5f5;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;

  label {
    display: block;
    font-size: 12px;
    color: #999;
    margin-bottom: 8px;
  }

  pre {
    margin: 0;
    font-size: 12px;
    font-family: 'Courier New', monospace;
    white-space: pre-wrap;
    color: #333;
  }
}

.method-description {
  font-size: 13px;
  color: #666;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #999;

  p {
    margin-bottom: 16px;
  }

  .create-btn {
    background-color: #f5f7fa;
    color: #666;

    &:hover {
      background-color: #e8ebf0;
    }
  }
}
</style>
