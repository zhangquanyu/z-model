<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { methodApi } from '@/api/method'
import { ArrowLeft, Document } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const modelId = route.params.id as string
const methodId = route.params.methodId as string
const method = ref<any>({})

onMounted(async () => {
  try {
    const methodRes = await methodApi.getById(modelId, methodId)
    method.value = methodRes
  } catch (error) {
    console.error('Failed to load method:', error)
  }
})

const handleBack = () => {
  router.push(`/models/${modelId}/methods`)
}

const handleEdit = () => {
  router.push(`/models/${modelId}/methods/${methodId}/edit`)
}

const handleViewRequirement = (requirementId: string) => {
  router.push(`/requirements/${requirementId}`)
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

const getParamNames = (params: any[]) => {
  return params?.map(p => p.name).join(', ') || '-'
}
</script>

<template>
  <div class="method-detail">
    <div class="detail-header">
      <button class="back-btn" @click="handleBack">
        <ArrowLeft />
        <span>返回</span>
      </button>
      <div class="header-info">
        <h1>{{ method.name }}</h1>
        <span class="method-code">{{ method.code }}</span>
      </div>
      <button class="edit-btn" @click="handleEdit">编辑方法</button>
    </div>

    <div class="detail-content">
      <div class="overview-section">
        <h3>基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">方法名称</span>
            <span class="info-value">{{ method.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">方法编码</span>
            <span class="info-value">{{ method.code }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ method.createdAt?.slice(0, 10) }}</span>
          </div>
        </div>
        <div class="description-box">
          <span class="info-label">子需求描述</span>
          <div v-html="method.description || '<span class=\'empty-text\'>暂无描述</span>'"></div>
        </div>
      </div>

      <div class="params-section">
        <h3>参数配置</h3>
        <div class="params-grid">
          <div class="param-card">
            <div class="param-header">
              <span class="param-title">入参</span>
              <span class="param-count">{{ method.inputParams?.length || 0 }} 个</span>
            </div>
            <div v-if="method.inputParams?.length > 0" class="param-list">
              <div v-for="param in method.inputParams" :key="'in-' + param.id" class="param-item">
                <span class="param-name">{{ param.name }}</span>
                <span class="param-type">{{ param.dataType }}</span>
              </div>
            </div>
            <div v-else class="empty-text">暂无入参</div>
          </div>
          <div class="param-card">
            <div class="param-header">
              <span class="param-title">出参</span>
              <span class="param-count">{{ method.outputParams?.length || 0 }} 个</span>
            </div>
            <div v-if="method.outputParams?.length > 0" class="param-list">
              <div v-for="param in method.outputParams" :key="'out-' + param.id" class="param-item">
                <span class="param-name">{{ param.name }}</span>
                <span class="param-type">{{ param.dataType }}</span>
              </div>
            </div>
            <div v-else class="empty-text">暂无出参</div>
          </div>
        </div>
      </div>

      <div class="requirements-section">
        <h3>关联需求</h3>
        <div v-if="method.parentRequirementIds?.length > 0" class="requirements-list">
          <div
            v-for="(reqId, index) in method.parentRequirementIds"
            :key="reqId"
            class="requirement-item"
            @click="handleViewRequirement(reqId)"
          >
            <Document class="req-icon" />
            <div class="req-info">
              <div class="req-name">{{ method.parentRequirementNames?.[index] || reqId }}</div>
              <div class="req-code">{{ method.parentRequirementCodes?.[index] || '' }}</div>
            </div>
            <span v-if="method.parentRequirementStatuses?.[index]" :class="'status-tag ' + method.parentRequirementStatuses[index].toLowerCase()">
              {{ getStatusText(method.parentRequirementStatuses[index]) }}
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
.method-detail {
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

  .method-code {
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
.params-section,
.requirements-section {
  background-color: #fafbfc;
  border-radius: 12px;
  padding: 20px;
}

.overview-section h3,
.params-section h3,
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

.params-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.param-card {
  background-color: white;
  border-radius: 10px;
  padding: 20px;
}

.param-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.param-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.param-count {
  font-size: 12px;
  color: #999;
  padding: 2px 8px;
  background-color: #f5f7fa;
  border-radius: 10px;
}

.param-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background-color: #fafbfc;
  border-radius: 8px;
}

.param-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.param-type {
  font-size: 12px;
  color: #666;
  padding: 2px 8px;
  background-color: #e8ebf0;
  border-radius: 10px;
}

.empty-text {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 20px;
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
