<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, ArrowLeft, Plus } from '@element-plus/icons-vue'
import { orchestrationApi, type Orchestration, type OrchestrationNode } from '@/api/orchestration'
import NodeTreeItem from './NodeTreeItem.vue'

const route = useRoute()
const router = useRouter()
const orchestrationId = route.params.id as string

const orchestration = ref<Orchestration | null>(null)
const loading = ref(false)
const activeTab = ref('info')

const statusTag = computed(() => {
  if (!orchestration.value) return { type: '', label: '' }
  const map: Record<string, { type: string; label: string }> = {
    'DRAFT': { type: 'info', label: '草稿' },
    'ACTIVE': { type: 'success', label: '已启用' },
    'ARCHIVED': { type: 'warning', label: '已归档' }
  }
  return map[orchestration.value.status || 'DRAFT']
})

const nodeTypeMap: Record<string, { label: string; color: string }> = {
  'SERIAL': { label: '串行', color: '#409EFF' },
  'PARALLEL': { label: '并行', color: '#67C23A' },
  'LOOP': { label: '循环', color: '#E6A23C' }
}

const loadOrchestration = async () => {
  loading.value = true
  try {
    const data = await orchestrationApi.getById(orchestrationId)
    orchestration.value = data
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleDesign = () => {
  router.push(`/orchestrations/${orchestrationId}/design`)
}

const handleEdit = () => {
  router.push(`/orchestrations/${orchestrationId}/edit`)
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该业务编排吗？删除后将同时删除所有编排节点和关联数据。', '提示', {
      type: 'warning'
    })
    await orchestrationApi.delete(orchestrationId)
    ElMessage.success('删除成功')
    router.push('/orchestrations')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBack = () => {
  router.push('/orchestrations')
}

onMounted(() => {
  loadOrchestration()
})

// 递归计算节点总数（包含子节点）
const totalNodeCount = computed(() => {
  if (!orchestration.value?.nodes) return 0
  const countNodes = (nodes: OrchestrationNode[]): number => {
    let count = nodes.length
    for (const node of nodes) {
      if (node.children && node.children.length > 0) {
        count += countNodes(node.children)
      }
    }
    return count
  }
  return countNodes(orchestration.value.nodes)
})

// 递归计算方法总数（包含子节点的方法）
const totalMethodCount = computed(() => {
  if (!orchestration.value?.nodes) return 0
  const countMethods = (nodes: OrchestrationNode[]): number => {
    let count = 0
    for (const node of nodes) {
      count += node.methods?.length || 0
      if (node.children && node.children.length > 0) {
        count += countMethods(node.children)
      }
    }
    return count
  }
  return countMethods(orchestration.value.nodes)
})
</script>

<template>
  <div class="orchestration-detail" v-loading="loading">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="handleBack">返回列表</el-button>
      <div class="header-info" v-if="orchestration">
        <h2>{{ orchestration.name }}</h2>
        <el-tag :type="statusTag.type">{{ statusTag.label }}</el-tag>
        <span class="code">{{ orchestration.code }}</span>
        <span class="version">版本 v{{ orchestration.version }}</span>
      </div>
      <div class="header-actions" v-if="orchestration">
        <el-button type="primary" :icon="Plus" @click="handleDesign">编排设计</el-button>
        <el-button type="warning" :icon="Edit" @click="handleEdit">编辑</el-button>
        <el-button type="danger" :icon="Delete" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <div class="detail-body" v-if="orchestration">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <div class="info-section">
            <h3>编排描述</h3>
            <p>{{ orchestration.description || '暂无描述' }}</p>

            <h3>统计信息</h3>
            <div class="stats-row">
              <div class="stat-item">
                <div class="stat-value">{{ totalNodeCount }}</div>
                <div class="stat-label">编排节点</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ totalMethodCount }}</div>
                <div class="stat-label">方法总数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ orchestration.requirements?.length || 0 }}</div>
                <div class="stat-label">关联子需求</div>
              </div>
            </div>

            <h3>创建信息</h3>
            <p>创建时间：{{ orchestration.createdAt ? new Date(orchestration.createdAt).toLocaleString('zh-CN') : '-' }}</p>
            <p>更新时间：{{ orchestration.updatedAt ? new Date(orchestration.updatedAt).toLocaleString('zh-CN') : '-' }}</p>
          </div>
        </el-tab-pane>

        <el-tab-pane label="编排结构" name="structure">
          <div class="structure-section">
            <div v-if="!orchestration.nodes || orchestration.nodes.length === 0" class="empty-state">
              <el-empty description="暂无编排节点，点击右上角「编排设计」添加" />
            </div>
            <div v-else class="nodes-list">
              <NodeTreeItem
                v-for="(node, index) in orchestration.nodes"
                :key="node.id || index"
                :node="node"
                :node-type-map="nodeTypeMap"
                :index="index"
                :level="0"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="关联需求" name="requirements">
          <div class="requirements-section">
            <div v-if="!orchestration.requirements || orchestration.requirements.length === 0">
              <el-empty description="暂无关联需求" />
            </div>
            <el-table v-else :data="orchestration.requirements" stripe>
              <el-table-column prop="code" label="需求编号" width="150" />
              <el-table-column prop="name" label="需求名称" min-width="150" />
              <el-table-column label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.requirementType === 'MAIN' ? '' : 'success'">
                    {{ row.requirementType === 'MAIN' ? '主需求' : '子需求' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" />
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.orchestration-detail {
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

    .stats-row {
      display: flex;
      gap: 24px;
      margin: 16px 0;

      .stat-item {
        text-align: center;
        padding: 16px 24px;
        background: #f5f7fa;
        border-radius: 8px;

        .stat-value {
          font-size: 28px;
          font-weight: 600;
          color: var(--primary-color);
        }

        .stat-label {
          color: #909399;
          font-size: 14px;
          margin-top: 4px;
        }
      }
    }
  }

  .structure-section {
    .nodes-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
  }

  .requirements-section {
    margin-top: 16px;
  }
}
</style>
