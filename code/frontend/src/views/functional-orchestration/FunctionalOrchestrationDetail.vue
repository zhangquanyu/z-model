<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { functionalOrchestrationApi, type FunctionalOrchestration, type GeneratedCodeResponse } from '@/api/functional-orchestration'
import { ArrowLeft, Edit, Delete, Setting, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string

const orchestration = ref<FunctionalOrchestration | null>(null)
const showCodeDialog = ref(false)
const generatedCode = ref<GeneratedCodeResponse | null>(null)
const activeCodeTab = ref('entity')

const loadData = async () => {
  try {
    orchestration.value = await functionalOrchestrationApi.getById(id)
  } catch (error) {
    console.error('Failed to load functional orchestration:', error)
  }
}

const handleEdit = () => {
  router.push(`/functional-orchestrations/${id}/edit`)
}

const handleDesign = () => {
  router.push(`/functional-orchestrations/${id}/design`)
}

const handleDelete = async () => {
  if (confirm('确定要删除这个功能编排吗？')) {
    try {
      await functionalOrchestrationApi.delete(id)
      router.push('/functional-orchestrations')
    } catch (error) {
      console.error('Failed to delete:', error)
    }
  }
}

const handleGoBack = () => {
  router.push('/functional-orchestrations')
}

const handleGenerateCode = async () => {
  try {
    generatedCode.value = await functionalOrchestrationApi.generateCode(id)
    showCodeDialog.value = true
    activeCodeTab.value = 'entity'
  } catch (error) {
    console.error('Failed to generate code:', error)
    ElMessage.error('生成代码失败')
  }
}

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败'))
}

const downloadCode = (filename: string, content: string) => {
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

const getNodeTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'DB_READ': '数据库读取',
    'DB_WRITE': '数据库写入',
    'API_CALL': 'API调用',
    'TRANSFORM': '数据转换',
    'CONDITION': '条件判断',
    'LOOP': '循环',
    'CUSTOM': '自定义'
  }
  return labels[type] || type
}

const getNodeTypeClass = (type: string) => {
  const classes: Record<string, string> = {
    'DB_READ': 'db-read',
    'DB_WRITE': 'db-write',
    'API_CALL': 'api-call',
    'TRANSFORM': 'transform',
    'CONDITION': 'condition',
    'LOOP': 'loop',
    'CUSTOM': 'custom'
  }
  return classes[type] || 'default'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="fo-detail" v-if="orchestration">
    <div class="page-header">
      <button class="back-btn" @click="handleGoBack">
        <ArrowLeft />
        返回
      </button>
      <h2 class="page-title">{{ orchestration.name }}</h2>
      <div class="header-actions">
        <button class="action-btn design" @click="handleDesign">
          <Setting />
          编排设计
        </button>
        <button class="action-btn generate" @click="handleGenerateCode">
          <Code />
          生成代码
        </button>
        <button class="action-btn edit" @click="handleEdit">
          <Edit />
          编辑
        </button>
        <button class="action-btn delete" @click="handleDelete">
          <Delete />
          删除
        </button>
      </div>
    </div>

    <div class="content-sections">
      <!-- 基本信息 -->
      <div class="section">
        <h3 class="section-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">功能编排名称</span>
            <span class="value">{{ orchestration.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">编号</span>
            <span class="value">{{ orchestration.code || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">关联业务编排</span>
            <span class="value">{{ orchestration.orchestrationId || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态</span>
            <span :class="['status-tag', orchestration.status?.toLowerCase() === 'completed' ? 'completed' : orchestration.status?.toLowerCase() === 'designing' ? 'designing' : 'draft']">
              {{ orchestration.status === 'COMPLETED' ? '已完成' : orchestration.status === 'DESIGNING' ? '设计中' : '草稿' }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">创建时间</span>
            <span class="value">{{ orchestration.createdAt?.replace('T', ' ').slice(0, 19) }}</span>
          </div>
          <div class="info-item full-width">
            <span class="label">描述</span>
            <span class="value">{{ orchestration.description || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 编排节点 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">编排节点 ({{ orchestration.nodes?.length || 0 }})</h3>
          <button class="section-action" @click="handleDesign">
            <Setting />
            进入编排设计
          </button>
        </div>
        <div class="nodes-list" v-if="orchestration.nodes && orchestration.nodes.length > 0">
          <div 
            v-for="(node, index) in orchestration.nodes" 
            :key="node.id" 
            class="node-item"
          >
            <div class="node-index">{{ index + 1 }}</div>
            <div class="node-content">
              <div class="node-header">
                <span :class="['node-type', getNodeTypeClass(node.nodeType)]">
                  {{ getNodeTypeLabel(node.nodeType) }}
                </span>
                <span class="node-label">{{ node.label || '未命名节点' }}</span>
              </div>
              <div class="node-detail" v-if="node.methods && node.methods.length > 0">
                <span class="detail-label">关联方法:</span>
                <span class="detail-value">{{ node.methods.map(m => m.methodId).join(', ') }}</span>
              </div>
              <div class="node-detail" v-if="node.nodeConfig">
                <span class="detail-label">配置:</span>
                <span class="detail-value">{{ node.nodeConfig.configKey }}: {{ node.nodeConfig.configValue }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p>暂无编排节点，点击"进入编排设计"开始设计</p>
        </div>
      </div>
    </div>

    <!-- 代码生成对话框 -->
    <el-dialog v-model="showCodeDialog" title="生成的脚手架代码" width="900px" max-height="85vh">
      <div class="code-tabs" v-if="generatedCode">
        <div class="tabs-header">
          <div 
            v-for="tab in ['entity', 'mapper', 'serviceInterface', 'service', 'controller', 'orchestration']" 
            :key="tab"
            :class="['tab', activeCodeTab === tab ? 'active' : '']"
            @click="activeCodeTab = tab"
          >
            {{ 
              {
                entity: 'Entity 实体类',
                mapper: 'Mapper 接口',
                serviceInterface: 'Service 接口',
                service: 'Service 实现',
                controller: 'Controller',
                orchestration: '编排逻辑'
              }[tab]
            }}
          </div>
        </div>
        <div class="tabs-content">
          <div v-if="activeCodeTab === 'entity'" class="code-content">
            <pre>{{ generatedCode.entityCode }}</pre>
            <div class="code-actions">
              <button @click="copyToClipboard(generatedCode.entityCode || '')">复制</button>
              <button @click="downloadCode('Entity.java', generatedCode.entityCode || '')">下载</button>
            </div>
          </div>
          <div v-if="activeCodeTab === 'mapper'" class="code-content">
            <pre>{{ generatedCode.mapperCode }}</pre>
            <div class="code-actions">
              <button @click="copyToClipboard(generatedCode.mapperCode || '')">复制</button>
              <button @click="downloadCode('Mapper.java', generatedCode.mapperCode || '')">下载</button>
            </div>
          </div>
          <div v-if="activeCodeTab === 'serviceInterface'" class="code-content">
            <pre>{{ generatedCode.serviceInterfaceCode }}</pre>
            <div class="code-actions">
              <button @click="copyToClipboard(generatedCode.serviceInterfaceCode || '')">复制</button>
              <button @click="downloadCode('Service.java', generatedCode.serviceInterfaceCode || '')">下载</button>
            </div>
          </div>
          <div v-if="activeCodeTab === 'service'" class="code-content">
            <pre>{{ generatedCode.serviceCode }}</pre>
            <div class="code-actions">
              <button @click="copyToClipboard(generatedCode.serviceCode || '')">复制</button>
              <button @click="downloadCode('ServiceImpl.java', generatedCode.serviceCode || '')">下载</button>
            </div>
          </div>
          <div v-if="activeCodeTab === 'controller'" class="code-content">
            <pre>{{ generatedCode.controllerCode }}</pre>
            <div class="code-actions">
              <button @click="copyToClipboard(generatedCode.controllerCode || '')">复制</button>
              <button @click="downloadCode('Controller.java', generatedCode.controllerCode || '')">下载</button>
            </div>
          </div>
          <div v-if="activeCodeTab === 'orchestration'" class="code-content">
            <pre>{{ generatedCode.orchestrationCode }}</pre>
            <div class="code-actions">
              <button @click="copyToClipboard(generatedCode.orchestrationCode || '')">复制</button>
              <button @click="downloadCode('Orchestration.java', generatedCode.orchestrationCode || '')">下载</button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCodeDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.fo-detail {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
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

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;

  svg {
    width: 16px;
    height: 16px;
  }

  &.design {
    background-color: #e8f5e9;
    color: #4caf50;

    &:hover {
      background-color: #c8e6c9;
    }
  }

  &.generate {
    background-color: #f3e5f5;
    color: #9c27b0;

    &:hover {
      background-color: #e1bee7;
    }
  }

  &.edit {
    background-color: #fff3e0;
    color: #ff9800;

    &:hover {
      background-color: #ffe0b2;
    }
  }

  &.delete {
    background-color: #ffebee;
    color: #f44336;

    &:hover {
      background-color: #ffcdd2;
    }
  }
}

.content-sections {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.section {
  background-color: #fafbfc;
  border-radius: 12px;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.section-action {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #1e3a5f;
    color: #1e3a5f;
  }

  svg {
    width: 16px;
    height: 16px;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &.full-width {
    grid-column: 1 / -1;
  }

  .label {
    font-size: 12px;
    color: #999;
  }

  .value {
    font-size: 14px;
    color: #333;
  }
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;

  &.draft {
    background-color: #f5f5f5;
    color: #999;
  }

  &.designing {
    background-color: #fff3e0;
    color: #ff9800;
  }

  &.completed {
    background-color: #e8f5e9;
    color: #4caf50;
  }
}

.nodes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.node-item {
  display: flex;
  gap: 16px;
  background-color: white;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.node-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background-color: #1e3a5f;
  color: white;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.node-content {
  flex: 1;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.node-type {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.db-read {
    background-color: #e3f2fd;
    color: #2196f3;
  }

  &.db-write {
    background-color: #e8f5e9;
    color: #4caf50;
  }

  &.api-call {
    background-color: #fff3e0;
    color: #ff9800;
  }

  &.transform {
    background-color: #f3e5f5;
    color: #9c27b0;
  }

  &.condition {
    background-color: #fff9c4;
    color: #fbc02d;
  }

  &.loop {
    background-color: #b3e5fc;
    color: #0277bd;
  }

  &.custom {
    background-color: #fce4ec;
    color: #c2185b;
  }
}

.node-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.node-detail {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: #666;
  margin-top: 4px;

  .detail-label {
    color: #999;
  }

  .detail-value {
    color: #333;
  }
}

.empty-state {
  text-align: center;
  padding: 32px;
  color: #999;
}

.code-tabs {
  .tabs-header {
    display: flex;
    gap: 4px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;
  }

  .tab {
    padding: 10px 16px;
    cursor: pointer;
    font-size: 14px;
    color: #666;
    border-bottom: 2px solid transparent;
    transition: all 0.2s;

    &.active {
      color: #1e3a5f;
      border-bottom-color: #1e3a5f;
      font-weight: 500;
    }

    &:hover {
      color: #1e3a5f;
    }
  }
}

.code-content {
  position: relative;
}

.code-content pre {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  font-size: 13px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  max-height: 50vh;
  overflow-x: auto;
  margin: 0;
}

.code-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;

  button {
    padding: 6px 12px;
    background-color: #1e3a5f;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 12px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #2d4a6f;
    }
  }
}
</style>
