<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { propertyApi } from '@/api/property'
import { requirementApi } from '@/api/requirement'
import { ArrowLeft, Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const modelId = route.params.id as string
const propertyId = route.params.propertyId as string
const property = ref<any>({})
const requirements = ref<any[]>([])

onMounted(async () => {
  try {
    const [propRes, reqRes] = await Promise.all([
      propertyApi.getById(modelId, propertyId),
      requirementApi.list({ page: 0, size: 100 })
    ])
    property.value = propRes
    requirements.value = reqRes.content || []
  } catch (error) {
    console.error('Failed to load property:', error)
  }
})

const getRequirementName = (id: string) => {
  const req = requirements.value.find(r => r.id === id)
  return req?.name || id
}

const getRequirementCode = (id: string) => {
  const req = requirements.value.find(r => r.id === id)
  return req?.code || ''
}

const getRequirementStatus = (id: string) => {
  const req = requirements.value.find(r => r.id === id)
  return req?.status || ''
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const handleBack = () => {
  router.push(`/models/${modelId}/properties`)
}

const handleEdit = () => {
  router.push(`/models/${modelId}/properties/${propertyId}/edit`)
}

const handleViewRequirement = (requirementId: string) => {
  router.push(`/requirements/${requirementId}`)
}

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
</script>

<template>
  <div class="property-detail">
    <div class="detail-header">
      <button class="back-btn" @click="handleBack">
        <ArrowLeft />
        <span>返回</span>
      </button>
      <div class="header-info">
        <h1>{{ property.name }}</h1>
        <span class="property-code">{{ property.code }}</span>
      </div>
      <button class="edit-btn" @click="handleEdit">编辑属性</button>
    </div>

    <div class="detail-content">
      <div class="overview-section">
        <h3>基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">属性名称</span>
            <span class="info-value">{{ property.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">属性编码</span>
            <span class="info-value">{{ property.code }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">数据类型</span>
            <span class="info-value">{{ getTypeName(property.dataType) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">是否必填</span>
            <span class="info-value">{{ property.required ? '是' : '否' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">默认值</span>
            <span class="info-value">{{ property.defaultValue || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ property.createdAt?.slice(0, 10) }}</span>
          </div>
        </div>
        <div class="description-box">
          <span class="info-label">子需求描述</span>
          <div v-html="property.description || '<span class=\'empty-text\'>暂无描述</span>'"></div>
        </div>
      </div>

      <div class="requirements-section">
        <h3>关联需求</h3>
        <div v-if="property.parentRequirementIds?.length > 0" class="requirements-list">
          <div
            v-for="reqId in property.parentRequirementIds"
            :key="reqId"
            class="requirement-item"
            @click="handleViewRequirement(reqId)"
          >
            <Document class="req-icon" />
            <div class="req-info">
              <div class="req-name">{{ getRequirementName(reqId) }}</div>
              <div class="req-code">{{ getRequirementCode(reqId) }}</div>
            </div>
            <span v-if="getRequirementStatus(reqId)" :class="'status-tag ' + getRequirementStatus(reqId).toLowerCase()">
              {{ getStatusText(getRequirementStatus(reqId)) }}
            </span>
          </div>
        </div>
        <div v-else class="empty-box">
          暂无关联需求
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.property-detail {
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

  h1 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }

  .property-code {
    font-size: 14px;
    color: #999;
  }
}

.edit-btn {
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
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.overview-section,
.requirements-section {
  background-color: #fafbfc;
  border-radius: 12px;
  padding: 20px;
}

.overview-section h3,
.requirements-section h3 {
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
</style>
