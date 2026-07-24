<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { methodApi } from '@/api/method'
import { modelApi } from '@/api/model'
import { propertyApi } from '@/api/property'
import { ArrowLeft, Check, Plus, Close } from '@element-plus/icons-vue'
import RichTextEditor from '@/components/RichTextEditor.vue'

const router = useRouter()
const route = useRoute()
const modelId = route.params.id as string
const isEdit = computed(() => !!route.params.methodId)

const form = ref({
  name: '',
  code: '',
  parentRequirementId: '',
  description: '',
  inputParams: [] as string[],
  outputParams: [] as string[]
})

const modelRequirements = ref<any[]>([])
const properties = ref<any[]>([])

onMounted(async () => {
  await loadModelRequirements()
  await loadProperties()
  
  if (isEdit.value && route.params.methodId) {
    try {
      const res = await methodApi.getById(modelId, route.params.methodId as string)
      form.value = {
        name: res.name || '',
        code: res.code || '',
        parentRequirementId: res.parentRequirementId || '',
        description: res.description || '',
        inputParams: res.inputParams?.map((p: any) => p.id) || [],
        outputParams: res.outputParams?.map((p: any) => p.id) || []
      }
    } catch (error) {
      console.error('Failed to load method:', error)
    }
  }
})

const loadModelRequirements = async () => {
  try {
    const res = await modelApi.getRequirements(modelId)
    modelRequirements.value = res || []
  } catch (error) {
    console.error('Failed to load model requirements:', error)
  }
}

const loadProperties = async () => {
  try {
    const res = await propertyApi.list(modelId)
    properties.value = res.content || []
  } catch (error) {
    console.error('Failed to load properties:', error)
  }
}

const availableInputProps = computed(() => {
  return properties.value.filter(p => !form.value.inputParams.includes(p.id) && !form.value.outputParams.includes(p.id))
})

const availableOutputProps = computed(() => {
  return properties.value.filter(p => !form.value.outputParams.includes(p.id) && !form.value.inputParams.includes(p.id))
})

const addInputParam = (id: string) => {
  if (!form.value.inputParams.includes(id)) {
    form.value.inputParams.push(id)
  }
}

const addOutputParam = (id: string) => {
  if (!form.value.outputParams.includes(id)) {
    form.value.outputParams.push(id)
  }
}

const removeInputParam = (id: string) => {
  const index = form.value.inputParams.indexOf(id)
  if (index > -1) {
    form.value.inputParams.splice(index, 1)
  }
}

const removeOutputParam = (id: string) => {
  const index = form.value.outputParams.indexOf(id)
  if (index > -1) {
    form.value.outputParams.splice(index, 1)
  }
}

const getPropertyName = (id: string) => {
  const prop = properties.value.find(p => p.id === id)
  return prop?.name || id
}

const getPropertyType = (id: string) => {
  const prop = properties.value.find(p => p.id === id)
  const map: Record<string, string> = {
    STRING: '字符串',
    INTEGER: '整数',
    LONG: '长整数',
    DOUBLE: '浮点数',
    BOOLEAN: '布尔值',
    DATE: '日期',
    DATETIME: '日期时间',
    ENUM: '枚举',
    OBJECT: '对象',
    ARRAY: '数组'
  }
  return map[prop?.dataType || ''] || prop?.dataType || ''
}

const handleSubmit = async (stay = false) => {
  try {
    const data = {
      name: form.value.name,
      code: form.value.code,
      parentRequirementId: form.value.parentRequirementId,
      description: form.value.description,
      inputParams: form.value.inputParams,
      outputParams: form.value.outputParams
    }
    
    if (isEdit.value && route.params.id) {
      await methodApi.update(modelId, route.params.id as string, data)
    } else {
      await methodApi.create(modelId, data)
    }
    
    if (!stay) {
      router.push(`/models/${modelId}/methods`)
    }
  } catch (error) {
    console.error('Failed to save method:', error)
  }
}

const handleBack = () => {
  router.push(`/models/${modelId}/methods`)
}
</script>

<template>
  <div class="method-form">
    <div class="form-header">
      <button class="back-btn" @click="handleBack">
        <ArrowLeft />
        <span>返回</span>
      </button>
      <h2>{{ isEdit ? '编辑方法' : '新建方法' }}</h2>
    </div>
    
    <div class="form-container">
      <div class="form-section">
        <h3>基本信息</h3>
        
        <div class="form-row">
          <div class="form-group">
            <label>方法名称 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              placeholder="请输入方法名称"
              required
            />
          </div>
          
          <div class="form-group">
            <label>方法编码</label>
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
            <label>关联主需求 <span class="required">*</span></label>
            <select v-model="form.parentRequirementId" required>
              <option value="">选择主需求</option>
              <option v-for="req in modelRequirements" :key="req.id" :value="req.id">
                {{ req.name }} ({{ req.code }})
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-group full-width">
          <label>子需求描述 <span class="required">*</span></label>
          <RichTextEditor v-model="form.description" placeholder="请输入子需求描述" />
        </div>
      </div>
      
      <div class="form-section">
        <h3>入参配置</h3>
        
        <div v-if="availableInputProps.length > 0" class="param-selector">
          <select @change="addInputParam(($event.target as HTMLSelectElement).value)" class="param-select">
            <option value="">选择属性作为入参</option>
            <option v-for="prop in availableInputProps" :key="prop.id" :value="prop.id">
              {{ prop.name }} ({{ getPropertyType(prop.id) }})
            </option>
          </select>
        </div>
        
        <div v-if="form.inputParams.length > 0" class="selected-params">
          <div v-for="id in form.inputParams" :key="'input-' + id" class="selected-item">
            <span>{{ getPropertyName(id) }}</span>
            <span class="param-type">{{ getPropertyType(id) }}</span>
            <button class="remove-btn" @click="removeInputParam(id)">
              <Close />
            </button>
          </div>
        </div>
        
        <div v-if="form.inputParams.length === 0" class="empty-hint">
          <Plus class="hint-icon" />
          <span>暂无入参，请从属性中选择</span>
        </div>
      </div>
      
      <div class="form-section">
        <h3>出参配置</h3>
        
        <div v-if="availableOutputProps.length > 0" class="param-selector">
          <select @change="addOutputParam(($event.target as HTMLSelectElement).value)" class="param-select">
            <option value="">选择属性作为出参</option>
            <option v-for="prop in availableOutputProps" :key="prop.id" :value="prop.id">
              {{ prop.name }} ({{ getPropertyType(prop.id) }})
            </option>
          </select>
        </div>
        
        <div v-if="form.outputParams.length > 0" class="selected-params">
          <div v-for="id in form.outputParams" :key="'output-' + id" class="selected-item">
            <span>{{ getPropertyName(id) }}</span>
            <span class="param-type">{{ getPropertyType(id) }}</span>
            <button class="remove-btn" @click="removeOutputParam(id)">
              <Close />
            </button>
          </div>
        </div>
        
        <div v-if="form.outputParams.length === 0" class="empty-hint">
          <Plus class="hint-icon" />
          <span>暂无出参，请从属性中选择</span>
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
.method-form {
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

.param-selector {
  margin-bottom: 20px;
}

.param-select {
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

.selected-params {
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
  
  span:first-child {
    font-size: 14px;
    color: #333;
  }
  
  .param-type {
    font-size: 12px;
    color: #666;
    padding: 2px 8px;
    background-color: rgba(0,0,0,0.05);
    border-radius: 10px;
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