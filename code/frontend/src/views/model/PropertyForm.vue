<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { propertyApi } from '@/api/property'
import { requirementApi } from '@/api/requirement'
import { ArrowLeft, Save } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const modelId = route.params.modelId as string
const isEdit = computed(() => !!route.params.id)

const form = ref({
  name: '',
  code: '',
  dataType: 'STRING',
  requirementId: '',
  required: false,
  defaultValue: '',
  description: ''
})

const requirements = ref<any[]>([])

const dataTypeOptions = [
  { value: 'STRING', label: '字符串' },
  { value: 'INTEGER', label: '整数' },
  { value: 'LONG', label: '长整数' },
  { value: 'DOUBLE', label: '浮点数' },
  { value: 'BOOLEAN', label: '布尔值' },
  { value: 'DATE', label: '日期' },
  { value: 'DATETIME', label: '日期时间' },
  { value: 'ENUM', label: '枚举' },
  { value: 'OBJECT', label: '对象' },
  { value: 'ARRAY', label: '数组' }
]

onMounted(async () => {
  await loadRequirements()
  
  if (isEdit.value && route.params.id) {
    try {
      const res = await propertyApi.get(route.params.id as string)
      form.value = {
        name: res.name || '',
        code: res.code || '',
        dataType: res.dataType || 'STRING',
        requirementId: res.requirement?.id || '',
        required: res.required || false,
        defaultValue: res.defaultValue || '',
        description: res.description || ''
      }
    } catch (error) {
      console.error('Failed to load property:', error)
    }
  }
})

const loadRequirements = async () => {
  try {
    const res = await requirementApi.list({ page: 0, size: 100 })
    requirements.value = res.content || []
  } catch (error) {
    console.error('Failed to load requirements:', error)
  }
}

const handleSubmit = async (stay = false) => {
  try {
    const data = {
      ...form.value,
      modelId,
      requirement: form.value.requirementId ? { id: form.value.requirementId } : null
    }
    
    if (isEdit.value && route.params.id) {
      await propertyApi.update(route.params.id as string, data)
    } else {
      await propertyApi.create(data)
    }
    
    if (!stay) {
      router.push(`/models/${modelId}/properties`)
    }
  } catch (error) {
    console.error('Failed to save property:', error)
  }
}

const handleBack = () => {
  router.push(`/models/${modelId}/properties`)
}
</script>

<template>
  <div class="property-form">
    <div class="form-header">
      <button class="back-btn" @click="handleBack">
        <ArrowLeft />
        <span>返回</span>
      </button>
      <h2>{{ isEdit ? '编辑属性' : '新建属性' }}</h2>
    </div>
    
    <div class="form-container">
      <div class="form-section">
        <h3>基本信息</h3>
        
        <div class="form-row">
          <div class="form-group">
            <label>属性名称 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              placeholder="请输入属性名称"
              required
            />
          </div>
          
          <div class="form-group">
            <label>属性编码</label>
            <input
              v-model="form.code"
              type="text"
              placeholder="系统自动生成"
              :readonly="isEdit"
            />
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>数据类型 <span class="required">*</span></label>
            <select v-model="form.dataType" required>
              <option v-for="opt in dataTypeOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label>关联需求</label>
            <select v-model="form.requirementId">
              <option value="">选择需求</option>
              <option v-for="req in requirements" :key="req.id" :value="req.id">
                {{ req.name }} ({{ req.code }})
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>是否必填</label>
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.required" />
              <span class="checkmark"></span>
              必填
            </label>
          </div>
          
          <div class="form-group">
            <label>默认值</label>
            <input
              v-model="form.defaultValue"
              type="text"
              placeholder="请输入默认值"
            />
          </div>
        </div>
        
        <div class="form-group full-width">
          <label>描述</label>
          <textarea
            v-model="form.description"
            rows="4"
            placeholder="请输入属性描述"
          ></textarea>
        </div>
      </div>
    </div>
    
    <div class="form-actions">
      <button class="btn btn-secondary" @click="handleBack">取消</button>
      <button class="btn btn-primary" @click="handleSubmit(false)">
        <Save />
        <span>保存并返回</span>
      </button>
      <button class="btn btn-outline" @click="handleSubmit(true)">
        <Save />
        <span>保存并继续</span>
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.property-form {
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
.form-group select,
.form-group textarea {
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

.form-group textarea {
  resize: vertical;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  
  input {
    display: none;
  }
  
  .checkmark {
    width: 20px;
    height: 20px;
    border: 2px solid #ddd;
    border-radius: 4px;
    position: relative;
    transition: all 0.2s ease;
    
    &::after {
      content: '';
      position: absolute;
      left: 6px;
      top: 2px;
      width: 5px;
      height: 10px;
      border: solid white;
      border-width: 0 2px 2px 0;
      transform: rotate(45deg);
      opacity: 0;
      transition: opacity 0.2s ease;
    }
  }
  
  input:checked + .checkmark {
    background-color: #1e3a5f;
    border-color: #1e3a5f;
    
    &::after {
      opacity: 1;
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
