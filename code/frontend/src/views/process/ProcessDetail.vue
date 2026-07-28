<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, ArrowLeft } from '@element-plus/icons-vue'
import BpmnViewer from 'bpmn-js'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import { processApi, type BpmnProcess } from '@/api/process'

const route = useRoute()
const router = useRouter()
const processId = route.params.id as string

const process = ref<BpmnProcess | null>(null)
const loading = ref(false)
const canvasRef = ref<HTMLElement | null>(null)
let viewer: BpmnViewer | null = null
const activeTab = ref('info')

const statusTag = computed(() => {
  if (!process.value) return { type: '', label: '' }
  const map: Record<string, { type: string; label: string }> = {
    'DRAFT': { type: 'info', label: '草稿' },
    'ACTIVE': { type: 'success', label: '已启用' },
    'ARCHIVED': { type: 'warning', label: '已归档' }
  }
  return map[process.value.status || 'DRAFT']
})

const loadProcess = async () => {
  loading.value = true
  try {
    const data = await processApi.getById(processId)
    process.value = data
    
    if (canvasRef.value && data?.bpmnXml) {
      viewer = new BpmnViewer({ container: canvasRef.value }) as any
      await (viewer as any).importXML(data.bpmnXml)
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  router.push(`/processes/${processId}/edit`)
}

const handleDesign = () => {
  router.push(`/processes/${processId}/design`)
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该流程吗？', '提示', {
      type: 'warning'
    })
    await processApi.delete(processId)
    ElMessage.success('删除成功')
    router.push('/processes')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBack = () => {
  router.push('/processes')
}

onMounted(() => {
  loadProcess()
})
</script>

<template>
  <div class="process-detail" v-loading="loading">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="handleBack">返回列表</el-button>
      <div class="header-info" v-if="process">
        <h2>{{ process.name }}</h2>
        <el-tag :type="statusTag.type">{{ statusTag.label }}</el-tag>
        <span class="code">{{ process.code }}</span>
        <span class="version">版本 v{{ process.version }}</span>
      </div>
      <div class="header-actions" v-if="process">
        <el-button type="primary" @click="handleDesign">设计流程</el-button>
        <el-button type="warning" :icon="Edit" @click="handleEdit">编辑</el-button>
        <el-button type="danger" :icon="Delete" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <div class="detail-body" v-if="process">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <div class="info-section">
            <h3>流程描述</h3>
            <p>{{ process.description || '暂无描述' }}</p>
            
            <h3>创建信息</h3>
            <p>创建时间：{{ process.createdAt ? new Date(process.createdAt).toLocaleString('zh-CN') : '-' }}</p>
            <p>更新时间：{{ process.updatedAt ? new Date(process.updatedAt).toLocaleString('zh-CN') : '-' }}</p>
          </div>
        </el-tab-pane>

        <el-tab-pane label="流程图" name="diagram">
          <div class="diagram-container" ref="canvasRef"></div>
        </el-tab-pane>

        <el-tab-pane label="模型绑定" name="bindings">
          <div class="bindings-section">
            <h3>节点模型绑定</h3>
            <el-table :data="process.nodeBindings || []" stripe>
              <el-table-column prop="nodeId" label="节点ID" width="200" />
              <el-table-column prop="modelName" label="模型名称" min-width="150" />
              <el-table-column prop="modelCode" label="模型编码" width="150" />
              <el-table-column prop="createdAt" label="绑定时间" width="180">
                <template #default="{ row }">
                  {{ row.createdAt ? new Date(row.createdAt).toLocaleString('zh-CN') : '-' }}
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!process.nodeBindings || process.nodeBindings.length === 0" description="暂无绑定" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="版本历史" name="versions">
          <div class="versions-section">
            <el-table :data="process.versions || []" stripe>
              <el-table-column prop="version" label="版本号" width="100" />
              <el-table-column prop="changeNote" label="变更说明" min-width="200" />
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ row.createdAt ? new Date(row.createdAt).toLocaleString('zh-CN') : '-' }}
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!process.versions || process.versions.length === 0" description="暂无版本记录" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.process-detail {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    background: white;
    padding: 16px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .header-info {
      display: flex;
      align-items: center;
      gap: 16px;

      h2 {
        margin: 0;
        color: var(--primary-color);
      }

      .code {
        color: #909399;
        font-size: 14px;
      }

      .version {
        color: #606266;
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .detail-body {
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .info-section {
      h3 {
        margin: 16px 0 8px;
        color: #303133;
      }

      p {
        margin: 4px 0;
        color: #606266;
      }
    }

    .diagram-container {
      height: 500px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      overflow: hidden;
    }

    .bindings-section, .versions-section {
      h3 {
        margin-bottom: 16px;
        color: #303133;
      }
    }
  }
}
</style>
