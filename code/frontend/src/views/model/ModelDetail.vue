<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { modelApi } from '@/api/model'
import { propertyApi } from '@/api/property'
import { methodApi } from '@/api/method'
import { ArrowLeft, Setting, Files, Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const modelId = route.params.id as string
const model = ref<any>({})
const activeTab = ref('properties')

const requirementId = computed(() => route.query.requirementId as string || '')
const isFromRequirement = computed(() => !!requirementId.value)

const filteredProperties = ref<any[]>([])
const filteredMethods = ref<any[]>([])

const loadFilteredProperties = async () => {
  if (!requirementId.value) return
  try {
    const res = await propertyApi.listByRequirement(modelId, requirementId.value)
    filteredProperties.value = res || []
  } catch (error) {
    console.error('Failed to load filtered properties:', error)
  }
}

const loadFilteredMethods = async () => {
  if (!requirementId.value) return
  try {
    const res = await methodApi.listByRequirement(modelId, requirementId.value)
    filteredMethods.value = res || []
  } catch (error) {
    console.error('Failed to load filtered methods:', error)
  }
}

onMounted(async () => {
  try {
    const res = await modelApi.getById(modelId)
    model.value = res
    if (isFromRequirement.value) {
      await loadFilteredProperties()
      await loadFilteredMethods()
    }
  } catch (error) {
    console.error('Failed to load model:', error)
  }
})

watch(() => route.query.requirementId, async () => {
  if (isFromRequirement.value) {
    await loadFilteredProperties()
    await loadFilteredMethods()
  }
})

const handleBack = () => {
  if (isFromRequirement.value && requirementId.value) {
    router.push(`/requirements/${requirementId.value}`)
  } else {
    router.push('/models')
  }
}

const handleManageProperties = () => {
  router.push(`/models/${modelId}/properties`)
}

const handleManageMethods = () => {
  router.push(`/models/${modelId}/methods`)
}

const handleViewRequirement = (requirementId: string) => {
  router.push(`/requirements/${requirementId}`)
}

const displayProperties = computed(() => {
  return isFromRequirement.value ? filteredProperties.value : model.value.properties || []
})

const displayMethods = computed(() => {
  return isFromRequirement.value ? filteredMethods.value : model.value.methods || []
})

const getTypeName = (type: string) => {
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
  return map[type] || type
}

const getParamNames = (params: any[]) => {
  return params?.map((p: any) => p.name).join(', ') || '-'
}
</script>

<template>
  <div class="model-detail">
    <div class="detail-header">
      <button class="back-btn" @click="handleBack">
        <ArrowLeft />
        <span>{{ isFromRequirement ? '返回需求详情' : '返回' }}</span>
      </button>
      <div class="header-info">
        <h1>{{ model.name }}</h1>
        <span class="model-code">{{ model.code }}</span>
      </div>
      <div v-if="isFromRequirement" class="context-badge">
        <Document class="context-icon" />
        <span>从需求视角查看</span>
      </div>
    </div>
    
    <div class="detail-content">
      <div class="overview-section">
        <h3>基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">模型名称</span>
            <span class="info-value">{{ model.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">模型编号</span>
            <span class="info-value">{{ model.code }}</span>
          </div>
          <div class="info-item" v-if="!isFromRequirement">
            <span class="info-label">关联需求</span>
            <span class="info-value">{{ model.requirements?.length || 0 }} 个</span>
          </div>
          <div class="info-item" v-else>
            <span class="info-label">当前需求</span>
            <span class="info-value">需求详情视角</span>
          </div>
          <div class="info-item">
            <span class="info-label">属性数量</span>
            <span class="info-value">{{ displayProperties.length }} 个</span>
          </div>
          <div class="info-item">
            <span class="info-label">方法数量</span>
            <span class="info-value">{{ displayMethods.length }} 个</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ model.createdAt?.slice(0, 10) }}</span>
          </div>
        </div>
        <div class="description-box">
          <span class="info-label">描述</span>
          <div v-html="model.description || '<span class=\'empty-text\'>暂无描述</span>'"></div>
        </div>
      </div>
      
      <div class="requirements-section" v-if="!isFromRequirement">
        <h3>关联需求</h3>
        <div v-if="model.requirements?.length > 0" class="requirements-list">
          <div 
            v-for="req in model.requirements" 
            :key="req.id" 
            class="requirement-item"
            @click="handleViewRequirement(req.id)"
          >
            <Document class="req-icon" />
            <div class="req-info">
              <div class="req-name">{{ req.name }}</div>
              <div class="req-code">{{ req.code }}</div>
            </div>
            <span :class="'status-tag ' + req.status.toLowerCase()">
              {{ req.status === 'DRAFT' ? '草稿' : req.status === 'PENDING' ? '待审批' : req.status === 'APPROVED' ? '已批准' : '已拒绝' }}
            </span>
          </div>
        </div>
        <div v-else class="empty-box">
          暂无关联需求
        </div>
      </div>
      
      <div class="tabs-section">
        <div class="tabs-header">
          <button 
            :class="['tab-btn', { active: activeTab === 'properties' }]" 
            @click="activeTab = 'properties'"
          >
            <Setting />
            <span>属性列表</span>
            <span v-if="isFromRequirement" class="tab-count">{{ displayProperties.length }}</span>
          </button>
          <button 
            :class="['tab-btn', { active: activeTab === 'methods' }]" 
            @click="activeTab = 'methods'"
          >
            <Files />
            <span>方法列表</span>
            <span v-if="isFromRequirement" class="tab-count">{{ displayMethods.length }}</span>
          </button>
        </div>
        
        <div v-if="activeTab === 'properties'" class="tab-content">
          <button class="manage-btn" @click="handleManageProperties">管理属性</button>
          
          <table class="detail-table">
            <thead>
              <tr>
                <th>属性名称</th>
                <th>属性编码</th>
                <th>数据类型</th>
                <th>必填</th>
                <th>默认值</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="prop in displayProperties" :key="prop.id">
                <td>{{ prop.name }}</td>
                <td>{{ prop.code }}</td>
                <td>{{ getTypeName(prop.dataType) }}</td>
                <td>{{ prop.required ? '是' : '否' }}</td>
                <td>{{ prop.defaultValue || '-' }}</td>
              </tr>
              <tr v-if="displayProperties.length === 0">
                <td colspan="5" class="empty-row">{{ isFromRequirement ? '该需求下暂无关联属性' : '暂无属性' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <div v-if="activeTab === 'methods'" class="tab-content">
          <button class="manage-btn" @click="handleManageMethods">管理方法</button>
          
          <table class="detail-table">
            <thead>
              <tr>
                <th>方法名称</th>
                <th>方法编码</th>
                <th>入参</th>
                <th>出参</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="method in displayMethods" :key="method.id">
                <td>{{ method.name }}</td>
                <td>{{ method.code }}</td>
                <td>{{ getParamNames(method.inputParams) }}</td>
                <td>{{ getParamNames(method.outputParams) }}</td>
              </tr>
              <tr v-if="displayMethods.length === 0">
                <td colspan="4" class="empty-row">{{ isFromRequirement ? '该需求下暂无关联方法' : '暂无方法' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.model-detail {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.detail-header {
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

.header-info {
  flex: 1;
  
 .detail-header h1 {
    font-size: 20px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }
  
  .model-code {
    font-size: 14px;
    color: #999;
  }
}

.context-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background-color: #e3f2fd;
  color: #1976d2;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.context-icon {
  font-size: 14px;
  width: 14px;
  height: 14px;
  
  svg {
    width: 14px !important;
    height: 14px !important;
  }
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.overview-section,
.requirements-section,
.tabs-section {
  background-color: #fafbfc;
  border-radius: 12px;
  padding: 20px;
}

.overview-section h3,
.requirements-section h3,
.tabs-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background-color: white;
  border-radius: 10px;
}

.info-label {
  font-size: 12px;
  color: #999;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.description-box {
  padding: 16px;
  background-color: white;
  border-radius: 10px;
  
  div {
    font-size: 14px;
    color: #666;
    margin-top: 8px;
    line-height: 1.8;
  }
  
  .empty-text {
    color: #999;
  }
}

.requirements-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.requirement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: white;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background-color: #f5f7fa;
    transform: translateX(4px);
  }
}

.req-icon {
  font-size: 16px;
  color: #667eea;
  width: 16px;
  height: 16px;
  
  svg {
    width: 16px !important;
    height: 16px !important;
  }
}

.req-info {
  flex: 1;
}

.req-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.req-code {
  font-size: 12px;
  color: #999;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  
  &.draft {
    background-color: #fff3e0;
    color: #ff9800;
  }
  
  &.pending {
    background-color: #e3f2fd;
    color: #2196f3;
  }
  
  &.approved {
    background-color: #e8f5e9;
    color: #4caf50;
  }
  
  &.rejected {
    background-color: #ffebee;
    color: #f44336;
  }
}

.empty-box {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 14px;
}

.tabs-header {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  background-color: white;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &.active {
    background-color: #1e3a5f;
    color: white;
    
    svg {
      color: white;
    }
  }
  
  &:not(.active):hover {
    background-color: #f0f2f5;
  }
  
  svg {
    font-size: 16px;
    color: #666;
  }
}

.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  margin-left: 4px;
}

.tab-content {
  background-color: white;
  border-radius: 10px;
  padding: 20px;
}

.manage-btn {
  float: right;
  padding: 8px 16px;
  background-color: #1e3a5f;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  margin-bottom: 16px;
  
  &:hover {
    background-color: #2d4a6f;
  }
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
}

.detail-table th,
.detail-table td {
  padding: 14px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.detail-table th {
  background-color: #fafbfc;
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.detail-table td {
  font-size: 14px;
  color: #666;
}

.empty-row {
  text-align: center;
  color: #999;
  padding: 30px;
}
</style>
