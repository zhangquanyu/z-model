<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { eventApi } from '@/api/event'
import { modelApi } from '@/api/model'
import { ArrowLeft, Check } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const isEdit = computed(() => !!route.params.id)

const form = ref({
  name: '',
  eventType: 'ORDER_CREATED',
  modelId: '',
  amount: 0,
  quantity: 0,
  status: 'SUCCESS',
  eventTime: new Date().toISOString().slice(0, 19),
  description: '',
  metadata: ''
})

const models = ref<any[]>([])

const eventTypeOptions = [
  { value: 'ORDER_CREATED', label: '订单创建' },
  { value: 'ORDER_PAID', label: '订单支付' },
  { value: 'ORDER_SHIPPED', label: '订单发货' },
  { value: 'ORDER_COMPLETED', label: '订单完成' },
  { value: 'PAYMENT_SUCCESS', label: '支付成功' },
  { value: 'PAYMENT_FAILED', label: '支付失败' },
  { value: 'REFUND_REQUESTED', label: '退款申请' },
  { value: 'REFUND_COMPLETED', label: '退款完成' },
  { value: 'USER_REGISTERED', label: '用户注册' },
  { value: 'USER_LOGIN', label: '用户登录' },
  { value: 'USER_LOGOUT', label: '用户登出' },
  { value: 'PRODUCT_VIEWED', label: '商品浏览' },
  { value: 'PRODUCT_ADDED', label: '商品加入购物车' },
  { value: 'INVENTORY_LOW', label: '库存预警' },
  { value: 'SYSTEM_ERROR', label: '系统错误' },
  { value: 'SYSTEM_WARNING', label: '系统警告' }
]

const statusOptions = [
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAILED', label: '失败' },
  { value: 'PENDING', label: '处理中' }
]

onMounted(async () => {
  await loadModels()
  
  if (isEdit.value && route.params.id) {
    try {
      const res = await eventApi.getById(Number(route.params.id))
      form.value = {
        name: res.name || '',
        eventType: res.eventType || 'ORDER_CREATED',
        modelId: res.model?.id || '',
        amount: res.amount || 0,
        quantity: res.quantity || 0,
        status: res.status || 'SUCCESS',
        eventTime: res.eventTime || new Date().toISOString().slice(0, 19),
        description: res.description || '',
        metadata: res.metadata || ''
      }
    } catch (error) {
      console.error('Failed to load event:', error)
    }
  }
})

const loadModels = async () => {
  try {
    const res = await modelApi.list({ page: 0, size: 100 })
    models.value = res.content || []
  } catch (error) {
    console.error('Failed to load models:', error)
  }
}

const handleSubmit = async (stay = false) => {
  try {
    const data = {
      ...form.value,
      model: form.value.modelId ? { id: form.value.modelId } : null,
      metadata: form.value.metadata ? JSON.parse(form.value.metadata) : {}
    }
    
    if (isEdit.value && route.params.id) {
      await eventApi.update(route.params.id as string, data)
    } else {
      await eventApi.create(data)
    }
    
    if (!stay) {
      router.push('/events')
    }
  } catch (error) {
    console.error('Failed to save event:', error)
  }
}

const handleBack = () => {
  router.push('/events')
}
</script>

<template>
  <div class="event-form">
    <div class="form-header">
      <button class="back-btn" @click="handleBack">
        <ArrowLeft />
        <span>返回</span>
      </button>
      <h2>{{ isEdit ? '编辑事件' : '登记事件' }}</h2>
    </div>
    
    <div class="form-container">
      <div class="form-section">
        <h3>基本信息</h3>
        
        <div class="form-row">
          <div class="form-group">
            <label>事件名称 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              placeholder="请输入事件名称"
              required
            />
          </div>
          
          <div class="form-group">
            <label>事件类型 <span class="required">*</span></label>
            <select v-model="form.eventType" required>
              <option v-for="opt in eventTypeOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>关联模型</label>
            <select v-model="form.modelId">
              <option value="">选择模型</option>
              <option v-for="model in models" :key="model.id" :value="model.id">
                {{ model.name }} ({{ model.code }})
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label>状态</label>
            <select v-model="form.status">
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>金额</label>
            <input
              v-model.number="form.amount"
              type="number"
              step="0.01"
              placeholder="0.00"
            />
          </div>
          
          <div class="form-group">
            <label>数量</label>
            <input
              v-model.number="form.quantity"
              type="number"
              step="1"
              placeholder="0"
            />
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>事件时间</label>
            <input
              v-model="form.eventTime"
              type="datetime-local"
            />
          </div>
        </div>
        
        <div class="form-group full-width">
          <label>描述</label>
          <textarea
            v-model="form.description"
            rows="4"
            placeholder="请输入事件描述"
          ></textarea>
        </div>
        
        <div class="form-group full-width">
          <label>元数据 (JSON格式)</label>
          <textarea
            v-model="form.metadata"
            rows="4"
            placeholder='{"key": "value"}'
          ></textarea>
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
.event-form {
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
  font-family: monospace;
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
