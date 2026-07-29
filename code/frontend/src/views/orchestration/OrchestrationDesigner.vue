<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Delete, Edit } from '@element-plus/icons-vue'
import { orchestrationApi, type Orchestration, type OrchestrationNode } from '@/api/orchestration'
import { modelApi, type Model } from '@/api/model'
import { methodApi, type Method } from '@/api/method'
import { requirementApi, type Requirement } from '@/api/requirement'

const route = useRoute()
const router = useRouter()
const orchestrationId = route.params.id as string

const orchestration = ref<Orchestration | null>(null)
const loading = ref(false)

const models = ref<Model[]>([])
const selectedModelId = ref<string>('')
const modelMethods = ref<Method[]>([])

const mainRequirements = ref<Requirement[]>([])

const nodeTypeMap: Record<string, { label: string; color: string; icon: string }> = {
  'SERIAL': { label: '串行', color: '#409EFF', icon: '→' },
  'PARALLEL': { label: '并行', color: '#67C23A', icon: '⇉' },
  'LOOP': { label: '循环', color: '#E6A23C', icon: '↻' }
}

const loadOrchestration = async () => {
  loading.value = true
  try {
    const data = await orchestrationApi.getById(orchestrationId)
    orchestration.value = data
  } catch (e) {
    ElMessage.error('加载编排失败')
  } finally {
    loading.value = false
  }
}

const loadModels = async () => {
  try {
    const res = await modelApi.list({ page: 0, size: 200 })
    models.value = res.content || []
  } catch (e) {
    console.error('加载模型列表失败', e)
  }
}

const loadMainRequirements = async () => {
  try {
    mainRequirements.value = await requirementApi.listMainRequirements('')
  } catch (e) {
    console.error('加载主需求列表失败', e)
  }
}

const loadModelMethods = async () => {
  if (!selectedModelId.value) {
    modelMethods.value = []
    return
  }
  try {
    const res = await methodApi.list(selectedModelId.value, { page: 0, size: 200 })
    modelMethods.value = res.content || []
  } catch (e) {
    console.error('加载模型方法失败', e)
    modelMethods.value = []
  }
}

const handleModelChange = () => {
  loadModelMethods()
}

const handleBack = () => {
  router.push(`/orchestrations/${orchestrationId}`)
}

const handleSave = async () => {
  if (!orchestration.value) return
  try {
    await orchestrationApi.update(orchestrationId, {
      name: orchestration.value.name,
      description: orchestration.value.description,
      status: orchestration.value.status
    })
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// ============ 节点操作 ============

const showAddNodeDialog = ref(false)
const newNodeType = ref('SERIAL')
const newNodeName = ref('')
const newNodeDesc = ref('')
const newNodeLoopCount = ref<number | null>(null)

const handleAddNode = () => {
  newNodeType.value = 'SERIAL'
  newNodeName.value = `节点-${(orchestration.value?.nodes?.length || 0) + 1}`
  newNodeDesc.value = ''
  newNodeLoopCount.value = null
  showAddNodeDialog.value = true
}

const confirmAddNode = async () => {
  try {
    await orchestrationApi.addNode(orchestrationId, {
      nodeType: newNodeType.value,
      nodeName: newNodeName.value,
      description: newNodeDesc.value || undefined,
      loopCount: newNodeType.value === 'LOOP' ? (newNodeLoopCount.value ?? undefined) : undefined
    })
    ElMessage.success('添加节点成功')
    showAddNodeDialog.value = false
    loadOrchestration()
  } catch (e) {
    ElMessage.error('添加节点失败')
  }
}

const editingNodeId = ref<string | null>(null)
const editNodeForm = ref({ nodeName: '', description: '', nodeType: 'SERIAL', loopCount: null as number | null })

const startEditNode = (node: OrchestrationNode) => {
  editingNodeId.value = node.id!
  editNodeForm.value = {
    nodeName: node.nodeName || '',
    description: node.description || '',
    nodeType: node.nodeType,
    loopCount: node.loopCount ?? null
  }
}

const confirmEditNode = async () => {
  if (!editingNodeId.value) return
  try {
    await orchestrationApi.updateNode(orchestrationId, editingNodeId.value, {
      nodeName: editNodeForm.value.nodeName,
      description: editNodeForm.value.description || undefined,
      nodeType: editNodeForm.value.nodeType,
      loopCount: editNodeForm.value.nodeType === 'LOOP' ? editNodeForm.value.loopCount ?? 1 : undefined
    })
    ElMessage.success('更新节点成功')
    editingNodeId.value = null
    loadOrchestration()
  } catch (e) {
    ElMessage.error('更新节点失败')
  }
}

const handleDeleteNode = async (nodeId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除该节点吗？节点下的所有方法绑定也会一并删除。', '提示', {
      type: 'warning'
    })
    await orchestrationApi.deleteNode(orchestrationId, nodeId)
    ElMessage.success('删除成功')
    loadOrchestration()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// ============ 方法操作 ============

const showAddMethodDialog = ref(false)
const targetNodeId = ref<string>('')
const selectedMethodId = ref<string>('')
const addMethodForm = ref({
  createNewRequirement: false,
  requirementId: '',
  parentRequirementId: '',
  newRequirementName: '',
  newRequirementDescription: ''
})

const openAddMethodDialog = (nodeId: string) => {
  targetNodeId.value = nodeId
  selectedMethodId.value = ''
  addMethodForm.value = {
    createNewRequirement: false,
    requirementId: '',
    parentRequirementId: '',
    newRequirementName: '',
    newRequirementDescription: ''
  }
  showAddMethodDialog.value = true
}

const confirmAddMethod = async () => {
  if (!selectedMethodId.value) {
    ElMessage.warning('请选择一个方法')
    return
  }

  const payload: any = {
    methodId: selectedMethodId.value
  }

  if (addMethodForm.value.createNewRequirement) {
    if (!addMethodForm.value.parentRequirementId) {
      ElMessage.warning('请选择父需求')
      return
    }
    if (!addMethodForm.value.newRequirementName) {
      ElMessage.warning('请输入子需求名称')
      return
    }
    payload.parentRequirementId = addMethodForm.value.parentRequirementId
    payload.newRequirementName = addMethodForm.value.newRequirementName
    payload.newRequirementDescription = addMethodForm.value.newRequirementDescription || undefined
  } else if (addMethodForm.value.requirementId) {
    payload.requirementId = addMethodForm.value.requirementId
  }

  try {
    await orchestrationApi.addNodeMethod(orchestrationId, targetNodeId.value, payload)
    ElMessage.success('添加方法成功')
    showAddMethodDialog.value = false
    loadOrchestration()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '添加方法失败')
  }
}

const handleRemoveMethod = async (nodeId: string, methodId: string) => {
  try {
    await ElMessageBox.confirm('确定要从节点中移除该方法吗？', '提示', {
      type: 'warning'
    })
    await orchestrationApi.removeNodeMethod(orchestrationId, nodeId, methodId)
    ElMessage.success('移除成功')
    loadOrchestration()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadOrchestration(), loadModels(), loadMainRequirements()])
})
</script>

<template>
  <div class="orchestration-designer" v-loading="loading">
    <div class="designer-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="handleBack">返回详情</el-button>
        <div class="orchestration-info" v-if="orchestration">
          <el-input
            v-model="orchestration.name"
            placeholder="编排名称"
            class="title-input"
            @change="handleSave"
          />
          <el-select
            v-model="orchestration.status"
            class="status-select"
            @change="handleSave"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已启用" value="ACTIVE" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddNode">
          <el-icon><Plus /></el-icon>
          添加节点
        </el-button>
      </div>
    </div>

    <div class="designer-body" v-if="orchestration">
      <div class="palette-panel">
        <div class="panel-title">方法选择器</div>
        <div class="model-selector">
          <el-select
            v-model="selectedModelId"
            placeholder="选择模型"
            filterable
            @change="handleModelChange"
            style="width: 100%"
          >
            <el-option
              v-for="model in models"
              :key="model.id"
              :label="model.name"
              :value="model.id"
            />
          </el-select>
        </div>
        <div class="methods-list" v-if="modelMethods.length > 0">
          <div
            v-for="m in modelMethods"
            :key="m.id"
            class="method-option"
            :class="{ selected: selectedMethodId === m.id }"
            @click="m.id && (selectedMethodId = m.id)"
          >
            <div class="method-name">{{ m.name }}</div>
            <div class="method-meta">
              <span class="method-code">{{ m.code }}</span>
              <span class="method-desc" v-if="m.description">{{ m.description }}</span>
            </div>
          </div>
        </div>
        <div v-else class="no-model-hint">
          <el-empty description="请选择模型以查看方法列表" :image-size="60" />
        </div>
      </div>

      <div class="canvas-area">
        <div v-if="!orchestration.nodes || orchestration.nodes.length === 0" class="empty-canvas">
          <el-empty description="暂无编排节点，点击「添加节点」开始设计" />
        </div>
        <div v-else class="flow-container">
          <div v-for="(node, index) in orchestration.nodes" :key="node.id" class="flow-node-wrapper">
            <div class="node-connector" v-if="index > 0">
              <div class="connector-line"></div>
              <div class="connector-label">
                {{ nodeTypeMap[orchestration.nodes[index - 1].nodeType]?.icon || '→' }}
              </div>
            </div>
            <div
              class="flow-node"
              :style="{ borderColor: nodeTypeMap[node.nodeType]?.color }"
            >
              <div class="node-toolbar">
                <el-tag
                  :style="{ backgroundColor: nodeTypeMap[node.nodeType]?.color, color: '#fff', border: 'none' }"
                  size="small"
                >
                  {{ nodeTypeMap[node.nodeType]?.label || node.nodeType }}
                </el-tag>
                <span class="node-title">{{ node.nodeName || `节点-${index + 1}` }}</span>
                <div class="node-actions">
                  <el-button link size="small" :icon="Edit" @click="startEditNode(node)">编辑</el-button>
                  <el-button link size="small" type="danger" :icon="Delete" @click="handleDeleteNode(node.id!)">删除</el-button>
                </div>
              </div>
              <div class="node-body">
                <div class="node-desc" v-if="node.description">{{ node.description }}</div>
                <div v-if="node.nodeType === 'LOOP'" class="loop-info">
                  循环次数: {{ node.loopCount || 1 }}
                </div>
                <div class="node-methods">
                  <div v-if="!node.methods || node.methods.length === 0" class="empty-methods">
                    点击下方按钮添加方法
                  </div>
                  <div v-for="m in node.methods" :key="m.id" class="method-block">
                    <div class="method-block-info">
                      <span class="method-block-name">{{ m.methodName }}</span>
                      <div class="method-block-tags">
                        <el-tag v-if="m.modelName" size="small" type="info">{{ m.modelName }}</el-tag>
                        <el-tag v-if="m.requirementName" size="small" type="success">
                          {{ m.requirementName }}
                        </el-tag>
                      </div>
                    </div>
                    <el-button
                      link
                      size="small"
                      type="danger"
                      :icon="Delete"
                      @click="handleRemoveMethod(node.id!, m.methodId!)"
                    />
                  </div>
                </div>
                <div class="add-method-btn">
                  <el-button type="primary" link :icon="Plus" @click="openAddMethodDialog(node.id!)">
                    添加方法
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showAddNodeDialog" title="添加编排节点" width="450px">
      <el-form :model="{ nodeType: newNodeType, nodeName: newNodeName, description: newNodeDesc, loopCount: newNodeLoopCount }" label-width="80px">
        <el-form-item label="节点类型">
          <el-select v-model="newNodeType" style="width: 100%">
            <el-option label="串行" value="SERIAL" />
            <el-option label="并行" value="PARALLEL" />
            <el-option label="循环" value="LOOP" />
          </el-select>
        </el-form-item>
        <el-form-item label="节点名称" required>
          <el-input v-model="newNodeName" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="节点描述">
          <el-input v-model="newNodeDesc" type="textarea" :rows="2" placeholder="请输入节点描述" />
        </el-form-item>
        <el-form-item v-if="newNodeType === 'LOOP'" label="循环次数">
          <el-input-number v-model="newNodeLoopCount" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddNodeDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddNode">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editingNodeId !== null" title="编辑编排节点" width="450px">
      <el-form :model="editNodeForm" label-width="80px">
        <el-form-item label="节点类型">
          <el-select v-model="editNodeForm.nodeType" style="width: 100%">
            <el-option label="串行" value="SERIAL" />
            <el-option label="并行" value="PARALLEL" />
            <el-option label="循环" value="LOOP" />
          </el-select>
        </el-form-item>
        <el-form-item label="节点名称" required>
          <el-input v-model="editNodeForm.nodeName" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="节点描述">
          <el-input v-model="editNodeForm.description" type="textarea" :rows="2" placeholder="请输入节点描述" />
        </el-form-item>
        <el-form-item v-if="editNodeForm.nodeType === 'LOOP'" label="循环次数">
          <el-input-number v-model="editNodeForm.loopCount" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editingNodeId = null">取消</el-button>
        <el-button type="primary" @click="confirmEditNode">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showAddMethodDialog"
      title="添加方法到节点"
      width="550px"
    >
      <div class="method-selector-dialog">
        <div class="step-section">
          <div class="step-title">1. 选择方法</div>
          <div v-if="selectedMethodId" class="selected-method">
            <span class="tag">已选方法ID: {{ selectedMethodId }}</span>
          </div>
          <div v-else class="no-selection">
            请在左侧面板中先选择一个模型和方法
          </div>
        </div>

        <el-divider />

        <div class="step-section">
          <div class="step-title">2. 关联需求（可选）</div>
          <el-switch
            v-model="addMethodForm.createNewRequirement"
            active-text="创建新子需求"
            inactive-text="使用已有需求"
          />

          <div v-if="addMethodForm.createNewRequirement" class="new-req-form">
            <el-form label-width="100px" size="small">
              <el-form-item label="父需求" required>
                <el-select
                  v-model="addMethodForm.parentRequirementId"
                  placeholder="选择父需求"
                  filterable
                  style="width: 100%"
                >
                  <el-option
                    v-for="req in mainRequirements"
                    :key="req.id"
                    :label="req.name + ' (' + req.code + ')'"
                    :value="req.id"
                  />
                </el-select>
                <div class="hint">新子需求将同时属于所选父需求和当前业务编排</div>
              </el-form-item>
              <el-form-item label="子需求名称" required>
                <el-input v-model="addMethodForm.newRequirementName" placeholder="请输入子需求名称" />
              </el-form-item>
              <el-form-item label="子需求描述">
                <el-input v-model="addMethodForm.newRequirementDescription" type="textarea" :rows="2" placeholder="请输入子需求描述" />
              </el-form-item>
            </el-form>
          </div>
          <div v-else class="existing-req-form">
            <el-select
              v-model="addMethodForm.requirementId"
              placeholder="选择已有子需求（可选）"
              filterable
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="req in mainRequirements"
                :key="req.id"
                :label="req.name + ' (' + req.code + ')'"
                :value="req.id"
              />
            </el-select>
            <div class="hint">选择已有需求可建立方法与需求的关联关系</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddMethodDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddMethod">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.orchestration-designer {
  .designer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .orchestration-info {
      display: flex;
      gap: 12px;
      align-items: center;

      .title-input {
        width: 240px;
        :deep(.el-input__wrapper) {
          box-shadow: none;
          border: 1px solid #e4e7ed;
        }
      }

      .status-select {
        width: 120px;
      }
    }
  }

  .designer-body {
    display: flex;
    gap: 20px;
    min-height: 600px;
  }

  .palette-panel {
    width: 280px;
    background: white;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    flex-shrink: 0;

    .panel-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;
      color: var(--primary-color);
    }

    .model-selector {
      margin-bottom: 16px;
    }

    .methods-list {
      max-height: 500px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .method-option {
      padding: 10px 12px;
      border: 1px solid #e4e7ed;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: var(--primary-color);
        background: #f0f7ff;
      }

      &.selected {
        border-color: var(--primary-color);
        background: #ecf5ff;
      }

      .method-name {
        font-weight: 500;
        font-size: 14px;
        margin-bottom: 4px;
      }

      .method-meta {
        font-size: 12px;
        color: #909399;
        display: flex;
        gap: 8px;
      }

      .method-code {
        font-family: monospace;
      }
    }

    .no-model-hint {
      padding: 20px 0;
    }
  }

  .canvas-area {
    flex: 1;
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    min-height: 500px;

    .empty-canvas {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
    }

    .flow-container {
      display: flex;
      flex-direction: column;
      gap: 0;
    }

    .flow-node-wrapper {
      display: flex;
      flex-direction: column;
    }

    .node-connector {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 32px;

      .connector-line {
        width: 2px;
        height: 100%;
        background: #dcdfe6;
        min-height: 16px;
      }

      .connector-label {
        position: absolute;
        background: white;
        padding: 2px 8px;
        color: #606266;
        font-size: 16px;
      }
    }

    .flow-node {
      border: 2px solid;
      border-radius: 10px;
      padding: 16px;
      background: #fafbfc;
      margin: 8px 0;

      .node-toolbar {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 12px;

        .node-title {
          font-weight: 600;
          font-size: 16px;
          flex: 1;
        }

        .node-actions {
          display: flex;
          gap: 4px;
        }
      }

      .node-body {
        padding-left: 4px;
      }

      .node-desc {
        color: #909399;
        font-size: 13px;
        margin-bottom: 8px;
      }

      .loop-info {
        background: #fdf6ec;
        color: #e6a23c;
        padding: 4px 10px;
        border-radius: 4px;
        font-size: 13px;
        display: inline-block;
        margin-bottom: 10px;
      }

      .node-methods {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .empty-methods {
        color: #c0c4cc;
        font-size: 13px;
        text-align: center;
        padding: 10px;
      }

      .method-block {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 12px;
        background: white;
        border-radius: 6px;
        border: 1px solid #ebeef5;

        .method-block-info {
          .method-block-name {
            font-weight: 500;
            font-size: 14px;
          }

          .method-block-tags {
            display: flex;
            gap: 6px;
            margin-top: 4px;
          }
        }
      }

      .add-method-btn {
        margin-top: 10px;
        text-align: center;
      }
    }
  }
}

.method-selector-dialog {
  .step-section {
    .step-title {
      font-weight: 600;
      margin-bottom: 12px;
      color: #303133;
    }

    .selected-method {
      padding: 10px;
      background: #ecf5ff;
      border-radius: 4px;
      font-size: 13px;

      .tag {
        color: #409eff;
      }
    }

    .no-selection {
      padding: 10px;
      background: #f5f7fa;
      border-radius: 4px;
      color: #909399;
      font-size: 13px;
    }

    .hint {
      margin-top: 6px;
      color: #909399;
      font-size: 12px;
    }
  }

  .new-req-form {
    margin-top: 12px;
  }

  .existing-req-form {
    margin-top: 12px;
  }
}
</style>
