<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { requirementApi } from '@/api/requirement'
import { ArrowLeft, Edit, View, Refresh, Box } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const requirement = ref<any>(null)
const relatedModels = ref<any[]>([])

const statusMap: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '待审批',
  APPROVED: '已批准',
  REJECTED: '已拒绝'
}

const priorityMap: Record<string, string> = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  CRITICAL: '紧急'
}

const isMainRequirement = computed(() => requirement.value?.requirementType === 'MAIN')

const loadRequirement = async (id: string) => {
  try {
    const res = await requirementApi.getById(id)
    requirement.value = res
  } catch (error) {
    console.error('Failed to load requirement:', error)
  }
}

const loadModels = async (id: string) => {
  try {
    const res = await requirementApi.getModelsByRequirement(id)
    relatedModels.value = res || []
  } catch (error) {
    console.error('Failed to load models:', error)
  }
}

const handleBack = () => {
  router.push('/requirements')
}

const handleEdit = () => {
  router.push(`/requirements/${route.params.id}/edit`)
}

const handleViewModel = (modelId: string) => {
  router.push(`/models/${modelId}?requirementId=${route.params.id}`)
}

const handleViewParentRequirement = (parentId: string) => {
  router.push(`/requirements/${parentId}`)
}

const initData = async () => {
  const id = route.params.id as string
  await loadRequirement(id)
  if (isMainRequirement.value) {
    await loadModels(id)
  } else {
    relatedModels.value = []
  }
}

onMounted(async () => {
  await initData()
})

watch(() => route.params.id, async (newId) => {
  if (newId) {
    await initData()
  }
})
</script>

<template>
  <div class="requirement-detail" v-if="requirement">
    <div class="detail-header">
      <button class="back-btn" @click="handleBack">
        <ArrowLeft />
        <span>返回列表</span>
      </button>
      <h2>{{ isMainRequirement ? '主需求详情' : '子需求详情' }}</h2>
      <button class="edit-btn" @click="handleEdit">
        <Edit />
        <span>编辑</span>
      </button>
    </div>
    
    <div class="detail-container">
      <div class="info-card">
        <div class="card-header">
          <h3>基本信息</h3>
        </div>
        <div class="card-body">
          <div class="info-row">
            <div class="info-item">
              <label>需求名称</label>
              <span class="info-value">{{ requirement.name }}</span>
            </div>
            <div class="info-item">
              <label>需求编号</label>
              <span class="info-value">{{ requirement.code }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <label>需求类型</label>
              <span :class="'type-badge ' + (isMainRequirement ? 'main' : 'sub')">
                {{ isMainRequirement ? '主需求' : '子需求' }}
              </span>
            </div>
            <div class="info-item" v-if="!isMainRequirement">
              <label>所属主需求</label>
              <span 
                v-if="requirement.parentName" 
                class="info-value link-value"
                @click="handleViewParentRequirement(requirement.parentId)"
              >
                {{ requirement.parentName }}
              </span>
              <span v-else class="info-value">-</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <label>状态</label>
              <span :class="'status-badge ' + requirement.status.toLowerCase()">
                {{ statusMap[requirement.status] || requirement.status }}
              </span>
            </div>
            <div class="info-item">
              <label>优先级</label>
              <span :class="'priority-badge ' + requirement.priority.toLowerCase()">
                {{ priorityMap[requirement.priority] || requirement.priority }}
              </span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <label>创建时间</label>
              <span class="info-value">{{ requirement.createdAt?.slice(0, 19).replace('T', ' ') }}</span>
            </div>
            <div class="info-item">
              <label>更新时间</label>
              <span class="info-value">{{ requirement.updatedAt?.slice(0, 19).replace('T', ' ') }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="info-card">
        <div class="card-header">
          <h3>需求描述</h3>
        </div>
        <div class="card-body">
          <div class="description-content" v-html="requirement.description || '<span class=\'empty-text\'>暂无描述</span>'"></div>
        </div>
      </div>
      
      <div class="info-card" v-if="isMainRequirement">
        <div class="card-header">
          <h3>关联模型列表</h3>
          <button class="refresh-btn" @click="loadModels">
            <Refresh />
          </button>
        </div>
        <div class="card-body">
          <div v-if="relatedModels.length === 0" class="empty-state">
            <p>暂无关联模型</p>
            <p class="empty-hint">可在模型管理中关联此需求</p>
          </div>
          <div v-else class="model-list">
            <div 
              v-for="model in relatedModels" 
              :key="model.id" 
              class="model-item"
              @click="handleViewModel(model.id)"
            >
              <div class="model-icon-wrapper">
                <Box class="model-icon" />
              </div>
              <div class="model-info">
                <div class="model-name">{{ model.name }}</div>
                <div class="model-code">{{ model.code }}</div>
              </div>
              <div class="model-action">
                <View class="view-icon" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.requirement-detail {
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

.detail-header h2 {
  flex: 1;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.edit-btn {
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
  transition: background-color 0.2s ease;
  
  &:hover {
    background-color: #2d4a6f;
  }
  
  svg {
    font-size: 16px;
  }
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  background-color: #fafbfc;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background-color: white;
  border-bottom: 1px solid #f0f0f0;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}

.refresh-btn {
  padding: 6px;
  background-color: #f5f7fa;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  
  svg {
    font-size: 16px;
    color: #666;
  }
}

.card-body {
  padding: 20px;
}

.info-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.link-value {
  color: #1e3a5f;
  cursor: pointer;
  text-decoration: underline;
  
  &:hover {
    color: #2d4a6f;
    text-decoration: none;
  }
}

.type-badge,
.status-badge,
.priority-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.type-badge.main {
  background-color: #e3f2fd;
  color: #1976d2;
}

.type-badge.sub {
  background-color: #e8f5e9;
  color: #388e3c;
}

.status-badge.draft {
  background-color: #fff3e0;
  color: #ff9800;
}

.status-badge.pending {
  background-color: #e3f2fd;
  color: #2196f3;
}

.status-badge.approved {
  background-color: #e8f5e9;
  color: #4caf50;
}

.status-badge.rejected {
  background-color: #ffebee;
  color: #f44336;
}

.priority-badge.low {
  background-color: #f5f5f5;
  color: #999;
}

.priority-badge.medium {
  background-color: #e3f2fd;
  color: #1976d2;
}

.priority-badge.high {
  background-color: #fff3e0;
  color: #ff9800;
}

.priority-badge.critical {
  background-color: #ffebee;
  color: #f44336;
}

.description-content {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  background-color: white;
  padding: 16px;
  border-radius: 8px;
  min-height: 100px;
  
  .empty-text {
    color: #999;
  }
}

.empty-state {
  text-align: center;
  padding: 40px;
  
  p {
    margin-bottom: 8px;
    color: #999;
    font-size: 14px;
  }
  
  .empty-hint {
    font-size: 12px;
    color: #bbb;
  }
}

.model-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.model-item {
  display: flex;
  align-items: center;
  gap: 16px;
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

.model-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: #e3f2fd;
  border-radius: 8px;
}

.model-icon {
  font-size: 20px;
  color: #1976d2;
  
  svg {
    width: 20px !important;
    height: 20px !important;
  }
}

.model-info {
  flex: 1;
}

.model-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.model-code {
  font-size: 12px;
  color: #999;
  font-family: monospace;
}

.model-action {
  display: flex;
  align-items: center;
}

.view-icon {
  font-size: 18px;
  color: #999;
}
</style>