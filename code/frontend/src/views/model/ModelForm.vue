<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { modelApi } from '@/api/model'
import { requirementApi } from '@/api/requirement'
import { ArrowLeft, Check, Plus, Close } from '@element-plus/icons-vue'
import RichTextEditor from '@/components/RichTextEditor.vue'

const router = useRouter()
const route = useRoute()
const isEdit = computed(() => !!route.params.id)

const form = ref({
  name: '',
  code: '',
  description: '',
  requirementIds: [] as string[]
})

const allRequirements = ref<any[]>([])

onMounted(async () => {
  await loadRequirements()
  
  if (isEdit.value && route.params.id) {
    try {
      const res = await modelApi.getById(route.params.id as string)
      form.value = {
        name: res.name || '',
        code: res.code || '',
        description: res.description || '',
        requirementIds: res.requirements?.map((r: any) => r.id) || []
      }
    } catch (error) {
      console.error('Failed to load model:', error)
    }
  }
})

const loadRequirements = async () => {
  try {
    const res = await requirementApi.listMainRequirements()
    allRequirements.value = res || []
  } catch (error) {
    console.error('Failed to load requirements:', error)
  }
}

const addRequirement = (id: string) => {
  if (!form.value.requirementIds.includes(id)) {
    form.value.requirementIds.push(id)
  }
}

const removeRequirement = (id: string) => {
  const index = form.value.requirementIds.indexOf(id)
  if (index > -1) {
    form.value.requirementIds.splice(index, 1)
  }
}

const getRequirementName = (id: string) => {
  const req = allRequirements.value.find(r => r.id === id)
  return req?.name || id
}

const availableRequirements = computed(() => {
  return allRequirements.value.filter(r => !form.value.requirementIds.includes(r.id))
})

const resetForm = () => {
  form.value = {
    name: '',
    code: '',
    description: '',
    requirementIds: [] as string[]
  }
}

const handleSubmit = async (stay = false) => {
  try {
    const data = {
      ...form.value,
      requirements: form.value.requirementIds.map(id => ({ id }))
    }
    
    if (isEdit.value && route.params.id) {
      await modelApi.update(route.params.id as string, data)
      if (!stay) {
        router.push('/models')
      }
    } else {
      await modelApi.create(data)
      if (stay) {
        // 保存并继续：重置表单，打开新的新建界面
        resetForm()
      } else {
        router.push('/models')
      }
    }
  } catch (error) {
    console.error('Failed to save model:', error)
  }
}

const handleBack = () => {
  router.push('/models')
}
</script>

<template>
  <div class="model-form">
    <div class="form-header">
      <button class="back-btn" @click="handleBack">
        <ArrowLeft />
        <span>返回</span>
      </button>
      <h2>{{ isEdit ? '编辑模型' : '新建模型' }}</h2>
    </div>
    
    <div class="form-container">
      <div class="form-section">
        <h3>基本信息</h3>
        
        <div class="form-row">
          <div class="form-group">
            <label>模型名称 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              placeholder="请输入模型名称"
              required
            />
          </div>
          
          <div class="form-group">
            <label>模型编号</label>
            <input
              v-model="form.code"
              type="text"
              placeholder="系统自动生成"
              :readonly="isEdit"
            />
          </div>
        </div>
        
        <div class="form-group full-width">
          <label>描述</label>
          <RichTextEditor v-model="form.description" placeholder="请输入模型描述" />
        </div>
      </div>
      
      <div class="form-section">
        <h3>关联主需求</h3>
        
        <div v-if="availableRequirements.length > 0" class="requirement-selector">
          <select @change="addRequirement(($event.target as HTMLSelectElement).value)" class="requirement-select">
            <option value="">选择主需求</option>
            <option v-for="req in availableRequirements" :key="req.id" :value="req.id">
              {{ req.name }} ({{ req.code }})
            </option>
          </select>
        </div>
        
        <div v-if="form.requirementIds.length > 0" class="selected-requirements">
          <div v-for="id in form.requirementIds" :key="id" class="selected-item">
            <span>{{ getRequirementName(id) }}</span>
            <button class="remove-btn" @click="removeRequirement(id)">
              <Close />
            </button>
          </div>
        </div>
        
        <div v-if="form.requirementIds.length === 0" class="empty-hint">
          <Plus class="hint-icon" />
          <span>暂无关联主需求，请选择主需求</span>
        </div>
      </div>
    </div>
    
    <div class="form-actions">
      <button class="btn btn-secondary" @click="handleBack">取消</button>
      <button class="btn btn-primary" @click="handleSubmit(false)">
        <Check />
        <span>保存并返回</span>
      </button>
      <button class="btn btn-outline" @click="handleSubmit(true)">
        <Check />
        <span>保存并继续</span>
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.model-form {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.form-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: #1e3a5f;
    color: #1e3a5f;
  }
  
  svg {
    font-size: 16px;
  }
}

.form-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.form-container {
  margin-bottom: 30px;
}

.form-section {
  margin-bottom: 30px;
}

.form-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #1e3a5f;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.full-width {
  grid-column: span 2;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  
  .required {
    color: #f44336;
  }
}

.form-group input,
.form-group select {
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease;
  
  &:focus {
    border-color: #1e3a5f;
  }
  
  &:read-only {
    background-color: #f5f7fa;
    color: #999;
  }
}

.requirement-selector {
  margin-bottom: 20px;
}

.requirement-select {
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 14px;
  width: 100%;
  outline: none;
  transition: border-color 0.2s ease;
  
  &:focus {
    border-color: #1e3a5f;
  }
}

.selected-requirements {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.selected-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background-color: #e8f5e9;
  border-radius: 20px;
  
  span {
    font-size: 14px;
    color: #333;
  }
  
  .remove-btn {
    padding: 4px;
    border: none;
    background: transparent;
    cursor: pointer;
    transition: color 0.2s ease;
    
    svg {
      font-size: 14px;
      color: #666;
    }
    
    &:hover svg {
      color: #f44336;
    }
  }
}

.empty-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 30px;
  background-color: #fafbfc;
  border-radius: 10px;
  color: #999;
  font-size: 14px;
  
  .hint-icon {
    font-size: 24px;
    width: 24px;
    height: 24px;
    
    & svg {
      width: 24px !important;
      height: 24px !important;
    }
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  svg {
    width: 16px !important;
    height: 16px !important;
  }
  
  &.btn-primary {
    background-color: #1e3a5f;
    color: white;
    border: none;
    
    &:hover {
      background-color: #2d4a6f;
    }
  }
  
  &.btn-secondary {
    background-color: #f5f7fa;
    color: #666;
    border: none;
    
    &:hover {
      background-color: #e8ebf0;
    }
  }
  
  &.btn-outline {
    background-color: white;
    color: #1e3a5f;
    border: 1px solid #1e3a5f;
    
    &:hover {
      background-color: #1e3a5f;
      color: white;
    }
  }
  
  svg {
    font-size: 16px;
  }
}
</style>