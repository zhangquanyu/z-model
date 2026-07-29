<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, ArrowLeft, Plus } from '@element-plus/icons-vue'
import { orchestrationApi, type Orchestration } from '@/api/orchestration'

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
                <div class="stat-value">{{ orchestration.nodes?.length || 0 }}</div>
                <div class="stat-label">编排节点</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">
                  {{ orchestration.nodes?.reduce((sum, n) => sum + (n.methods?.length || 0), 0) || 0 }}
                </div>
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
              <div v-for="(node, index) in orchestration.nodes" :key="node.id" class="node-card">
                <div class="node-header">
                  <span class="node-index">{{ index + 1 }}</span>
                  <el-tag :style="{ backgroundColor: nodeTypeMap[node.nodeType]?.color, color: '#fff', border: 'none' }">
                    {{ nodeTypeMap[node.nodeType]?.label || node.nodeType }}
                  </el-tag>
                  <span class="node-name">{{ node.nodeName || `节点-${index + 1}` }}</span>
                  <el-tag v-if="node.nodeType === 'LOOP'" type="warning" size="small">
                    循环次数: {{ node.loopCount || 1 }}
                  </el-tag>
                </div>
                <div class="node-description" v-if="node.description">
                  {{ node.description }}
                </div>
                <div class="node-methods">
                  <div v-if="!node.methods || node.methods.length === 0" class="no-methods">
                    未绑定方法
                  </div>
                  <div v-for="method in node.methods" :key="method.id" class="method-item">
                    <div class="method-info">
                      <span class="method-name">{{ method.methodName || '未知方法' }}</span>
                      <span class="method-code">{{ method.methodCode }}</span>
                    </div>
                    <div class="method-meta">
                      <el-tag v-if="method.modelName" size="small" type="info">{{ method.modelName }}</el-tag>
                      <el-tag v-if="method.requirementName" size="small" type="success">{{ method.requirementName }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
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
      gap: 16px;
    }

    .node-card {
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      padding: 16px;
      background: #fafbfc;

      .node-header {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;

        .node-index {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          background: var(--primary-color);
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          font-weight: 600;
        }

        .node-name {
          font-weight: 500;
          font-size: 16px;
        }
      }

      .node-description {
        color: #909399;
        font-size: 13px;
        margin-bottom: 8px;
      }

      .node-methods {
        display: flex;
        flex-direction: column;
        gap: 8px;
        padding-left: 38px;
      }

      .no-methods {
        padding-left: 38px;
        color: #c0c4cc;
        font-size: 13px;
      }

      .method-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 12px;
        background: white;
        border-radius: 4px;
        border: 1px solid #ebeef5;

        .method-info {
          display: flex;
          align-items: center;
          gap: 10px;

          .method-name {
            font-weight: 500;
          }

          .method-code {
            color: #909399;
            font-size: 13px;
          }
        }

        .method-meta {
          display: flex;
          gap: 6px;
        }
      }
    }
  }

  .requirements-section {
    margin-top: 16px;
  }
}
</style>
