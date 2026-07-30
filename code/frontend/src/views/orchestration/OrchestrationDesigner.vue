<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Delete, Edit, Operation, CircleCheck } from '@element-plus/icons-vue'
import { orchestrationApi } from '@/api/orchestration'
import { modelApi, type Model, type Method as ModelMethod } from '@/api/model'
import { requirementApi, type Requirement } from '@/api/requirement'

const route = useRoute()
const router = useRouter()
const orchestrationId = route.params.id as string

const loading = ref(false)

// ============ 本地设计状态 ============
interface DesignMethod {
  id?: string
  methodId: string
  methodName?: string
  modelId?: string
  modelName?: string
  requirementId?: string
  requirementName?: string
  subRequirementId?: string
  sortOrder?: number
}

interface DesignNode {
  id?: string
  nodeName: string
  description?: string
  nodeType: string
  sortOrder: number
  loopCount?: number
  methods: DesignMethod[]
}

const designData = reactive({
  id: '' as string,
  name: '' as string,
  code: '' as string,
  description: '' as string,
  status: 'DRAFT' as string,
  nodes: [] as DesignNode[]
})

const selectedNodeId = ref<string | null>(null)

const selectedNode = computed(() => {
  if (!selectedNodeId.value) return null
  return designData.nodes.find(n => n.id === selectedNodeId.value) || null
})

const nodeTypeMap: Record<string, { label: string; color: string; icon: string; desc: string }> = {
  SERIAL: { label: '串行', color: '#409EFF', icon: '→', desc: '按顺序依次执行' },
  PARALLEL: { label: '并行', color: '#67C23A', icon: '⇉', desc: '同时执行所有方法' },
  LOOP: { label: '循环', color: '#E6A23C', icon: '↻', desc: '重复执行N次' }
}

// ============ 数据加载 ============
const models = ref<Model[]>([])
const mainRequirements = ref<Requirement[]>([])
const modelMethodsCache = reactive<Record<string, ModelMethod[]>>({})

const loadOrchestration = async () => {
  loading.value = true
  try {
    const data = await orchestrationApi.getById(orchestrationId)
    designData.id = data.id!
    designData.name = data.name || ''
    designData.code = data.code || ''
    designData.description = data.description || ''
    designData.status = data.status || 'DRAFT'
    designData.nodes = (data.nodes || []).map((n, idx) => ({
      id: n.id,
      nodeName: n.nodeName || `节点-${idx + 1}`,
      description: n.description,
      nodeType: n.nodeType,
      sortOrder: n.sortOrder ?? idx,
      loopCount: n.loopCount,
      methods: (n.methods || []).map(m => ({
        id: m.id,
        methodId: m.methodId,
        methodName: m.methodName,
        modelId: m.modelId,
        modelName: m.modelName,
        requirementId: m.requirementId,
        requirementName: m.requirementName,
        sortOrder: m.sortOrder
      }))
    }))
    sortNodesByOrder()
  } catch (e) {
    ElMessage.error('加载编排失败')
  } finally {
    loading.value = false
  }
}

const loadModels = async () => {
  try {
    models.value = await modelApi.listAll()
  } catch (e) {
    console.error('加载模型列表失败', e)
  }
}

const loadMainRequirements = async () => {
  try {
    const res = await requirementApi.listMainRequirements({ keyword: '', size: 1000 })
    mainRequirements.value = res.content || []
  } catch (e) {
    console.error('加载主需求列表失败', e)
  }
}

const normalizeMethod = (m: ModelMethod): ModelMethod => {
  if (!m.requirementId && m.requirementIds && m.requirementIds.length > 0) {
    m.requirementId = m.requirementIds[0]
  }
  if (!m.requirementName && m.requirementNames && m.requirementNames.length > 0) {
    m.requirementName = m.requirementNames[0]
  }
  if (!m.parentRequirementId && m.parentRequirementIds && m.parentRequirementIds.length > 0) {
    m.parentRequirementId = m.parentRequirementIds[0]
  }
  if (!m.parentRequirementName && m.parentRequirementNames && m.parentRequirementNames.length > 0) {
    m.parentRequirementName = m.parentRequirementNames[0]
  }
  return m
}

const loadModelMethods = async (modelId: string) => {
  if (modelMethodsCache[modelId]) return modelMethodsCache[modelId]
  try {
    const res = await modelApi.getById(modelId)
    modelMethodsCache[modelId] = (res.methods || []).map(normalizeMethod)
    return modelMethodsCache[modelId]
  } catch (e) {
    console.error('加载模型方法失败', e)
    modelMethodsCache[modelId] = []
    return []
  }
}

// ============ 画布操作 ============
let tempIdCounter = 0
const genTempId = () => `temp-${Date.now()}-${tempIdCounter++}`

const sortNodesByOrder = () => {
  designData.nodes.sort((a, b) => a.sortOrder - b.sortOrder)
  designData.nodes.forEach((n, i) => { n.sortOrder = i })
}

const addNode = (nodeType: string) => {
  const newNode: DesignNode = {
    id: genTempId(),
    nodeName: `${nodeTypeMap[nodeType].label}节点-${designData.nodes.length + 1}`,
    description: '',
    nodeType,
    sortOrder: designData.nodes.length,
    loopCount: nodeType === 'LOOP' ? 1 : undefined,
    methods: []
  }
  designData.nodes.push(newNode)
  selectedNodeId.value = newNode.id!
}

const deleteNode = (nodeId: string) => {
  const idx = designData.nodes.findIndex(n => n.id === nodeId)
  if (idx === -1) return
  designData.nodes.splice(idx, 1)
  sortNodesByOrder()
  if (selectedNodeId.value === nodeId) {
    selectedNodeId.value = designData.nodes.length > 0 ? designData.nodes[0].id! : null
  }
}

const selectNode = (nodeId: string) => {
  selectedNodeId.value = nodeId
}

const moveNodeUp = (nodeId: string) => {
  const idx = designData.nodes.findIndex(n => n.id === nodeId)
  if (idx <= 0) return
  const temp = designData.nodes[idx]
  designData.nodes[idx] = designData.nodes[idx - 1]
  designData.nodes[idx - 1] = temp
  sortNodesByOrder()
}

const moveNodeDown = (nodeId: string) => {
  const idx = designData.nodes.findIndex(n => n.id === nodeId)
  if (idx === -1 || idx >= designData.nodes.length - 1) return
  const temp = designData.nodes[idx]
  designData.nodes[idx] = designData.nodes[idx + 1]
  designData.nodes[idx + 1] = temp
  sortNodesByOrder()
}

// ============ 左侧方法库状态 ============
const libraryModelId = ref<string>('')
const librarySelectedMethodId = ref<string>('')

const onLibraryModelChange = async () => {
  if (!libraryModelId.value) {
    librarySelectedMethodId.value = ''
    return
  }
  librarySelectedMethodId.value = ''
  if (!modelMethodsCache[libraryModelId.value]) {
    await loadModelMethods(libraryModelId.value)
  }
}

// ============ 方法操作 ============
const showMethodDialog = ref(false)
const methodDialogNodeId = ref<string>('')
const methodDialogMethods = ref<ModelMethod[]>([])
const methodDialogSelectedMethodId = ref<string>('')

const openMethodDialog = (nodeId: string) => {
  methodDialogNodeId.value = nodeId
  const modelId = libraryModelId.value || models.value[0]?.id || ''
  methodDialogMethods.value = [...(modelMethodsCache[modelId] || [])]
  methodDialogSelectedMethodId.value = ''
  showMethodDialog.value = true
}

const confirmAddMethodToNode = async () => {
  if (!methodDialogSelectedMethodId.value || !methodDialogNodeId.value) return
  const method = methodDialogMethods.value.find(m => m.id === methodDialogSelectedMethodId.value)
  if (!method) return

  const node = designData.nodes.find(n => n.id === methodDialogNodeId.value)
  if (!node) return

  const exists = node.methods.some(m => m.methodId === method.id)
  if (exists) {
    ElMessage.warning('该方法已存在于当前节点中')
    return
  }

  node.methods.push({
    methodId: method.id!,
    methodName: method.name,
    modelId: method.modelId,
    modelName: method.modelName,
    requirementId: method.requirementId,
    requirementName: method.requirementName,
    sortOrder: node.methods.length
  })

  showMethodDialog.value = false
  methodDialogSelectedMethodId.value = ''
  ElMessage.success('方法已添加到节点')
}

const removeMethodFromNode = (nodeId: string, methodId: string) => {
  const node = designData.nodes.find(n => n.id === nodeId)
  if (!node) return
  const idx = node.methods.findIndex(m => m.methodId === methodId)
  if (idx !== -1) node.methods.splice(idx, 1)
}

// ============ 拖拽操作 ============
const draggingMethod = ref<ModelMethod | null>(null)
const draggingModelId = ref<string>('')
const dragOverNodeId = ref<string | null>(null)
const dragEnterCount = ref<Record<string, number>>({})

// 节点内方法拖拽排序
const draggingNodeMethod = ref<{ nodeId: string; methodId: string } | null>(null)
const dragOverMethodIndex = ref<{ nodeId: string; index: number } | null>(null)
const methodDragEnterCount = ref<Record<string, number>>({})

const onDragStart = (method: ModelMethod, modelId: string) => {
  draggingMethod.value = { ...method }
  draggingModelId.value = modelId
  dragEnterCount.value = {}
}

const onDragEnd = () => {
  draggingMethod.value = null
  draggingModelId.value = ''
  dragOverNodeId.value = null
  dragEnterCount.value = {}
}

const onDragOverNode = (e: DragEvent, nodeId: string) => {
  e.preventDefault()
  if (draggingMethod.value) {
    dragOverNodeId.value = nodeId
    dragEnterCount.value[nodeId] = (dragEnterCount.value[nodeId] || 0) + 1
  }
}

const onDragLeaveNode = (nodeId: string) => {
  dragEnterCount.value[nodeId] = (dragEnterCount.value[nodeId] || 0) - 1
  if (dragEnterCount.value[nodeId] <= 0) {
    delete dragEnterCount.value[nodeId]
    if (dragOverNodeId.value === nodeId) {
      dragOverNodeId.value = null
    }
  }
}

const onCanvasDragOver = (e: DragEvent) => {
  e.preventDefault()
}

const onCanvasDrop = (e: DragEvent) => {
  e.preventDefault()
}

// 节点内方法排序拖拽
const onMethodDragStart = (nodeId: string, methodId: string) => {
  draggingNodeMethod.value = { nodeId, methodId }
  methodDragEnterCount.value = {}
}

const onMethodDragEnd = () => {
  draggingNodeMethod.value = null
  dragOverMethodIndex.value = null
  methodDragEnterCount.value = {}
}

const onMethodDragOver = (e: DragEvent, nodeId: string, index: number) => {
  e.preventDefault()
  e.stopPropagation()
  if (!draggingNodeMethod.value) return

  const key = `${nodeId}-${index}`
  methodDragEnterCount.value[key] = (methodDragEnterCount.value[key] || 0) + 1
  dragOverMethodIndex.value = { nodeId, index }
}

const onMethodDragLeave = (nodeId: string, index: number) => {
  const key = `${nodeId}-${index}`
  methodDragEnterCount.value[key] = (methodDragEnterCount.value[key] || 0) - 1
  if (methodDragEnterCount.value[key] <= 0) {
    delete methodDragEnterCount.value[key]
  }
}

const onMethodDrop = (e: DragEvent, nodeId: string, targetIndex: number) => {
  e.preventDefault()
  e.stopPropagation()
  if (!draggingNodeMethod.value || draggingNodeMethod.value.nodeId !== nodeId) {
    onMethodDragEnd()
    return
  }

  const node = designData.nodes.find(n => n.id === nodeId)
  if (!node) {
    onMethodDragEnd()
    return
  }

  const sourceIndex = node.methods.findIndex(m => m.methodId === draggingNodeMethod.value!.methodId)
  if (sourceIndex === -1) {
    onMethodDragEnd()
    return
  }

  const item = node.methods.splice(sourceIndex, 1)[0]
  const adjustedIndex = sourceIndex < targetIndex ? targetIndex - 1 : targetIndex
  node.methods.splice(adjustedIndex, 0, item)

  node.methods.forEach((m, i) => { m.sortOrder = i })

  onMethodDragEnd()
}

const getModelName = (modelId?: string) => {
  if (!modelId) return ''
  return models.value.find((m: Model) => m.id === modelId)?.name || ''
}

const addSelectedMethodToNode = (nodeId: string) => {
  if (!librarySelectedMethodId.value) return
  const node = designData.nodes.find(n => n.id === nodeId)
  if (!node) return

  const method = modelMethodsCache[libraryModelId.value]?.find(m => m.id === librarySelectedMethodId.value)
  if (!method) return

  const exists = node.methods.some(m => m.methodId === method.id)
  if (exists) {
    ElMessage.warning('该方法已存在于当前节点中')
    return
  }

  node.methods.push({
    methodId: method.id!,
    methodName: method.name,
    modelId: method.modelId || libraryModelId.value,
    modelName: method.modelName || getModelName(libraryModelId.value),
    requirementId: method.requirementId,
    requirementName: method.requirementName,
    sortOrder: node.methods.length
  })
  librarySelectedMethodId.value = ''
  ElMessage.success('方法已添加')
}

const onDropNode = (e: DragEvent, nodeId: string) => {
  e.preventDefault()
  const method = draggingMethod.value
  if (!method) return

  const node = designData.nodes.find(n => n.id === nodeId)
  if (!node) return

  const exists = node.methods.some(m => m.methodId === method.id)
  if (exists) {
    ElMessage.warning('该方法已存在于当前节点中')
    onDragEnd()
    return
  }

  node.methods.push({
    methodId: method.id!,
    methodName: method.name,
    modelId: method.modelId || draggingModelId.value,
    modelName: method.modelName || getModelName(draggingModelId.value),
    requirementId: method.requirementId,
    requirementName: method.requirementName,
    sortOrder: node.methods.length
  })

  onDragEnd()
  ElMessage.success('方法已拖入节点')
}

// ============ 子需求创建弹窗 ============
const showSubReqDialog = ref(false)
const subReqForm = reactive({
  methodId: '',
  methodName: '',
  modelName: '',
  parentRequirementId: '',
  subRequirementName: '',
  subRequirementCode: '',
  description: ''
})
const subReqTargetNodeId = ref<string>('')
const subReqTempMethodId = ref<string>('')

const openSubReqDialog = (nodeId: string, method: ModelMethod) => {
  subReqTargetNodeId.value = nodeId
  subReqForm.methodId = method.id!
  subReqForm.methodName = method.name
  subReqForm.modelName = method.modelName || ''
  subReqForm.parentRequirementId = method.requirementId || method.parentRequirementId || ''
  subReqForm.subRequirementName = ''
  subReqForm.subRequirementCode = ''
  subReqForm.description = ''
  subReqTempMethodId.value = genTempId()
  showSubReqDialog.value = true
}

const parentRequirementName = computed(() => {
  if (!subReqForm.parentRequirementId) return ''
  const req = mainRequirements.value.find(r => r.id === subReqForm.parentRequirementId)
  return req ? `${req.name} (${req.code})` : ''
})

const confirmCreateSubRequirement = async () => {
  if (!subReqForm.subRequirementName) {
    ElMessage.warning('请输入子需求名称')
    return
  }
  if (!subReqForm.parentRequirementId) {
    ElMessage.warning('请选择父需求')
    return
  }

  try {
    const node = designData.nodes.find(n => n.id === subReqTargetNodeId.value)
    if (!node) return

    const exists = node.methods.some(m => m.methodId === subReqForm.methodId)
    if (exists) {
      ElMessage.warning('该方法已存在于当前节点中')
      showSubReqDialog.value = false
      return
    }

    node.methods.push({
      id: subReqTempMethodId.value,
      methodId: subReqForm.methodId,
      methodName: subReqForm.methodName,
      modelName: subReqForm.modelName,
      requirementId: subReqForm.parentRequirementId,
      requirementName: subReqForm.subRequirementName,
      subRequirementId: '',
      sortOrder: node.methods.length
    })

    ElMessage.success('子需求已创建并关联')
    showSubReqDialog.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  }
}

// ============ 保存 ============
const isSaving = ref(false)

const handleSave = async () => {
  isSaving.value = true
  try {
    const payload = {
      name: designData.name,
      code: designData.code,
      description: designData.description,
      status: designData.status,
      nodes: designData.nodes.map(n => ({
        id: n.id?.startsWith('temp-') ? null : n.id,
        nodeName: n.nodeName,
        description: n.description,
        nodeType: n.nodeType,
        sortOrder: n.sortOrder,
        loopCount: n.loopCount,
        methods: n.methods.map(m => {
          const isTemp = m.id?.startsWith('temp-')
          const hasSubReqId = !!(m.subRequirementId && m.subRequirementId !== '')
          const isNewSubReq = !isTemp ? false : (!hasSubReqId && !!m.requirementName)

          const methodPayload: any = {
            id: isTemp ? null : m.id,
            methodId: m.methodId,
            sortOrder: m.sortOrder
          }

          if (isNewSubReq) {
            methodPayload.parentRequirementId = m.requirementId || null
            methodPayload.subRequirementName = m.requirementName || null
            methodPayload.subRequirementDescription = null
          } else if (hasSubReqId) {
            methodPayload.subRequirementId = m.subRequirementId
          } else if (m.requirementId) {
            methodPayload.requirementId = m.requirementId
          }

          return methodPayload
        })
      }))
    }
    await orchestrationApi.saveDesign(orchestrationId, payload)
    ElMessage.success('保存成功')
    loadOrchestration()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    isSaving.value = false
  }
}

const handleBack = () => {
  router.push(`/orchestrations/${orchestrationId}`)
}

onMounted(async () => {
  await Promise.all([loadOrchestration(), loadModels(), loadMainRequirements()])
  if (models.value.length > 0) {
    for (const m of models.value) {
      if (m.methods && m.methods.length > 0) {
        modelMethodsCache[m.id!] = (m.methods as ModelMethod[]).map(normalizeMethod)
      }
    }
    libraryModelId.value = models.value[0].id || ''
  }
})
</script>

<template>
  <div class="designer-container" v-loading="loading">
    <!-- 顶部工具栏 -->
    <div class="top-toolbar">
      <div class="toolbar-left">
        <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
        <el-divider direction="vertical" />
        <el-input
          v-model="designData.name"
          placeholder="编排名称"
          class="title-input"
        />
        <el-select
          v-model="designData.status"
          class="status-select"
        >
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已启用" value="ACTIVE" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" :icon="Edit" :loading="isSaving" @click="handleSave">
          保存设计
        </el-button>
      </div>
    </div>

    <div class="designer-main">
      <!-- 左侧面板 -->
      <div class="left-panel">
        <div class="panel-section">
          <div class="panel-section-title">
            <el-icon><Operation /></el-icon>
            节点类型
          </div>
          <div class="node-type-list">
            <div
              v-for="(info, type) in nodeTypeMap"
              :key="type"
              class="node-type-card"
              :style="{ borderColor: info.color }"
              @click="addNode(type)"
            >
              <div class="ntc-icon" :style="{ backgroundColor: info.color }">
                {{ info.icon }}
              </div>
              <div class="ntc-info">
                <div class="ntc-label">{{ info.label }}</div>
                <div class="ntc-desc">{{ info.desc }}</div>
              </div>
              <el-icon class="ntc-add"><Plus /></el-icon>
            </div>
          </div>
        </div>

        <div class="panel-section">
          <div class="panel-section-title">
            <el-icon><CircleCheck /></el-icon>
            方法库
          </div>
          <div class="method-library">
            <div class="library-tip">💡 可拖拽方法到画布节点</div>
            <el-select
              v-model="libraryModelId"
              placeholder="选择模型"
              filterable
              style="width: 100%; margin-bottom: 12px"
              @change="onLibraryModelChange"
            >
              <el-option
                v-for="model in models"
                :key="model.id"
                :label="model.name"
                :value="model.id"
              />
            </el-select>
            <div class="library-methods" v-if="modelMethodsCache[libraryModelId]?.length">
              <div
                v-for="m in modelMethodsCache[libraryModelId]"
                :key="m.id"
                class="library-method-item"
                :class="{ selected: librarySelectedMethodId === m.id, dragging: draggingMethod?.id === m.id }"
                :draggable="!!m.id"
                @click="m.id && (librarySelectedMethodId = m.id)"
                @dragstart="m.id && onDragStart(m, libraryModelId)"
                @dragend="onDragEnd"
              >
                <div class="lm-drag-handle" v-if="m.id" title="拖拽到节点">⋮⋮</div>
                <div class="lm-content">
                  <div class="lm-name">{{ m.name }}</div>
                  <div class="lm-meta">
                    <span v-if="m.code" class="lm-code">{{ m.code }}</span>
                    <span v-if="m.requirementName" class="lm-req">{{ m.requirementName }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else-if="libraryModelId" class="library-empty">
              该模型暂无方法
            </div>
            <div v-else class="library-empty">
              请选择模型查看方法
            </div>
          </div>
        </div>
      </div>

      <!-- 中间画布 -->
      <div class="center-canvas" @dragover="onCanvasDragOver" @drop="onCanvasDrop">
        <div v-if="designData.nodes.length === 0" class="empty-canvas">
          <el-empty description="点击左侧「节点类型」开始搭建业务流程">
            <el-button type="primary" @click="addNode('SERIAL')">创建第一个节点</el-button>
          </el-empty>
        </div>

        <div v-else class="canvas-content">
          <template v-for="(node, index) in designData.nodes" :key="node.id">
            <!-- 连接线 -->
            <div v-if="index > 0" class="canvas-connector">
              <div class="connector-arrow" :style="{ color: nodeTypeMap[designData.nodes[index - 1].nodeType]?.color }">
                {{ nodeTypeMap[designData.nodes[index - 1].nodeType]?.icon || '→' }}
              </div>
              <div class="connector-line" :style="{ background: nodeTypeMap[designData.nodes[index - 1].nodeType]?.color }"></div>
            </div>

            <!-- 节点卡片 -->
            <div
              class="canvas-node"
              :class="{ selected: selectedNodeId === node.id, 'drag-over': dragOverNodeId === node.id }"
              :style="{ borderColor: nodeTypeMap[node.nodeType]?.color }"
              @click="selectNode(node.id!)"
              @dragover="(e) => onDragOverNode(e, node.id!)"
              @dragleave="onDragLeaveNode(node.id!)"
              @drop="(e) => onDropNode(e, node.id!)"
            >
              <div class="node-header" :style="{ borderBottomColor: nodeTypeMap[node.nodeType]?.color }">
                <div class="node-header-left">
                  <span
                    class="node-type-tag"
                    :style="{ backgroundColor: nodeTypeMap[node.nodeType]?.color }"
                  >
                    {{ nodeTypeMap[node.nodeType]?.label }}
                  </span>
                  <span class="node-name">{{ node.nodeName }}</span>
                </div>
                <div class="node-header-actions" @click.stop>
                  <el-button
                    link
                    size="small"
                    :icon="Delete"
                    type="danger"
                    @click="deleteNode(node.id!)"
                  />
                </div>
              </div>

              <div class="node-content">
                <div v-if="node.description" class="node-desc">{{ node.description }}</div>
                <div v-if="node.nodeType === 'LOOP'" class="node-loop">
                  循环 <strong>{{ node.loopCount || 1 }}</strong> 次
                </div>

                <!-- 方法列表 -->
                <div class="node-methods">
                  <template v-if="node.nodeType === 'PARALLEL' && node.methods.length > 0">
                    <div class="parallel-branch">
                      <div
                        v-for="(m, idx) in node.methods"
                        :key="m.id || m.methodId"
                        class="method-branch-item"
                        :class="{ 'method-drag-over': dragOverMethodIndex?.nodeId === node.id && dragOverMethodIndex?.index === idx }"
                        :draggable="node.methods.length > 1"
                        @dragstart="onMethodDragStart(node.id!, m.methodId)"
                        @dragend="onMethodDragEnd"
                        @dragover="(e) => onMethodDragOver(e, node.id!, idx)"
                        @dragleave="onMethodDragLeave(node.id!, idx)"
                        @drop="(e) => onMethodDrop(e, node.id!, idx)"
                      >
                        <div class="branch-line"></div>
                        <div class="branch-method">
                          <span class="bm-drag-handle" v-if="node.methods.length > 1">⋮⋮</span>
                          <span class="bm-name">{{ m.methodName }}</span>
                          <el-tag v-if="m.modelName" size="small" type="info">{{ m.modelName }}</el-tag>
                        </div>
                      </div>
                    </div>
                  </template>
                  <template v-else-if="node.methods.length > 0">
                    <div
                      v-for="(m, idx) in node.methods"
                      :key="m.id || m.methodId"
                      class="method-row"
                      :class="{ 'method-drag-over': dragOverMethodIndex?.nodeId === node.id && dragOverMethodIndex?.index === idx, 'dragging-row': draggingNodeMethod?.nodeId === node.id && draggingNodeMethod?.methodId === m.methodId }"
                      :draggable="node.methods.length > 1"
                      @dragstart="onMethodDragStart(node.id!, m.methodId)"
                      @dragend="onMethodDragEnd"
                      @dragover="(e) => onMethodDragOver(e, node.id!, idx)"
                      @dragleave="onMethodDragLeave(node.id!, idx)"
                      @drop="(e) => onMethodDrop(e, node.id!, idx)"
                    >
                      <span class="mr-drag-handle" v-if="node.methods.length > 1">⋮⋮</span>
                      <span v-if="node.nodeType === 'SERIAL' && node.methods.length > 1" class="method-order">
                        {{ idx + 1 }}.
                      </span>
                      <span class="mr-name">{{ m.methodName }}</span>
                      <el-tag v-if="m.modelName" size="small" type="info">{{ m.modelName }}</el-tag>
                      <el-button
                        link
                        size="small"
                        type="danger"
                        :icon="Delete"
                        @click="removeMethodFromNode(node.id!, m.methodId)"
                      />
                    </div>
                  </template>
                  <div v-else class="node-empty-methods">
                    暂无方法
                  </div>
                </div>

                <div class="node-footer">
                  <el-button
                    type="primary"
                    link
                    :icon="Plus"
                    @click.stop="openMethodDialog(node.id!)"
                  >
                    添加方法
                  </el-button>
                  <el-tooltip
                    v-if="librarySelectedMethodId && node.methods.findIndex(m => m.methodId === librarySelectedMethodId) === -1"
                    content="点击确认添加选中方法，或直接拖拽方法到节点"
                    placement="top"
                  >
                    <el-button
                      type="success"
                      link
                      @click.stop="addSelectedMethodToNode(node.id!)"
                    >
                      + 添加「{{ modelMethodsCache[libraryModelId]?.find(m => m.id === librarySelectedMethodId)?.name }}」
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="right-panel" v-if="selectedNode">
        <div class="panel-section">
          <div class="panel-section-title">
            <el-icon><Edit /></el-icon>
            属性设置
          </div>

          <div class="property-form">
            <div class="property-item">
              <label>节点类型</label>
              <el-tag
                :style="{ backgroundColor: nodeTypeMap[selectedNode.nodeType]?.color, color: '#fff', border: 'none' }"
                size="large"
              >
                {{ nodeTypeMap[selectedNode.nodeType]?.label }}
              </el-tag>
            </div>

            <div class="property-item">
              <label>节点名称</label>
              <el-input v-model="selectedNode.nodeName" placeholder="请输入节点名称" />
            </div>

            <div class="property-item">
              <label>节点描述</label>
              <el-input
                v-model="selectedNode.description"
                type="textarea"
                :rows="3"
                placeholder="请输入节点描述"
              />
            </div>

            <div v-if="selectedNode.nodeType === 'LOOP'" class="property-item">
              <label>循环次数</label>
              <el-input-number v-model="selectedNode.loopCount" :min="1" :max="100" />
            </div>

            <div class="property-item">
              <label>执行顺序</label>
              <div class="order-actions">
                <el-button size="small" :disabled="selectedNode.sortOrder === 0" @click="moveNodeUp(selectedNode.id!)">
                  ↑ 上移
                </el-button>
                <el-button
                  size="small"
                  :disabled="selectedNode.sortOrder === designData.nodes.length - 1"
                  @click="moveNodeDown(selectedNode.id!)"
                >
                  ↓ 下移
                </el-button>
              </div>
            </div>

            <div class="property-item">
              <label>方法列表 ({{ selectedNode.methods.length }})</label>
              <div class="property-method-list">
                <div
                  v-for="(m, idx) in selectedNode.methods"
                  :key="m.id || m.methodId"
                  class="pm-item"
                >
                  <span class="pm-order">{{ idx + 1 }}</span>
                  <span class="pm-name">{{ m.methodName }}</span>
                  <el-tag v-if="m.modelName" size="small" type="info">{{ m.modelName }}</el-tag>
                  <el-button
                    link
                    size="small"
                    type="danger"
                    :icon="Delete"
                    @click="removeMethodFromNode(selectedNode.id!, m.methodId)"
                  />
                </div>
                <div v-if="selectedNode.methods.length === 0" class="pm-empty">
                  暂无方法，点击「添加方法」开始
                </div>
              </div>
              <el-button
                type="primary"
                :icon="Plus"
                style="width: 100%; margin-top: 8px"
                @click="openMethodDialog(selectedNode.id!)"
              >
                添加方法
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="right-panel right-panel-empty" v-else>
        <el-empty description="选择画布中的节点以编辑属性" :image-size="80" />
      </div>
    </div>

    <!-- 方法选择弹窗 -->
    <el-dialog
      v-model="showMethodDialog"
      title="选择方法"
      width="500px"
    >
      <div class="method-dialog">
        <div v-if="methodDialogMethods.length > 0" class="method-dialog-list">
          <div
            v-for="m in methodDialogMethods"
            :key="m.id"
            class="method-dialog-item"
            :class="{ selected: methodDialogSelectedMethodId === m.id }"
            @click="m.id && (methodDialogSelectedMethodId = m.id)"
          >
            <div class="mdi-main">
              <div class="mdi-name">{{ m.name }}</div>
              <div class="mdi-meta">
                <span v-if="m.code" class="mdi-code">{{ m.code }}</span>
                <span v-if="m.description" class="mdi-desc">{{ m.description }}</span>
              </div>
            </div>
            <el-icon v-if="methodDialogSelectedMethodId === m.id" class="mdi-check"><Edit /></el-icon>
          </div>
        </div>
        <div v-else class="method-dialog-empty">
          请选择一个模型以查看方法列表
        </div>

        <el-divider v-if="methodDialogSelectedMethodId" />

        <div v-if="methodDialogSelectedMethodId" class="sub-req-section">
          <div class="sub-req-title">关联需求 (可选)</div>
          <div class="sub-req-options">
            <el-button type="success" plain size="small" @click="() => {
              const m = methodDialogMethods.find(x => x.id === methodDialogSelectedMethodId)
              if (m) openSubReqDialog(methodDialogNodeId, m)
            }">
              + 创建子需求
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showMethodDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!methodDialogSelectedMethodId" @click="confirmAddMethodToNode">
          添加到节点
        </el-button>
      </template>
    </el-dialog>

    <!-- 子需求创建弹窗 -->
    <el-dialog
      v-model="showSubReqDialog"
      title="创建子需求"
      width="480px"
    >
      <div class="sub-req-dialog">
        <div class="req-info-row">
          <div class="req-info-item">
            <span class="ri-label">关联方法：</span>
            <span class="ri-value">{{ subReqForm.methodName }}</span>
            <span v-if="subReqForm.modelName" class="ri-model">(模型: {{ subReqForm.modelName }})</span>
          </div>
        </div>

        <el-form label-width="90px" size="default">
          <el-form-item label="父需求" required>
            <el-select
              v-model="subReqForm.parentRequirementId"
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
          </el-form-item>
          <el-form-item label="子需求名称" required>
            <el-input v-model="subReqForm.subRequirementName" placeholder="请输入子需求名称" />
          </el-form-item>
          <el-form-item label="子需求编码">
            <el-input v-model="subReqForm.subRequirementCode" placeholder="留空自动生成" />
          </el-form-item>
          <el-form-item label="需求描述">
            <el-input
              v-model="subReqForm.description"
              type="textarea"
              :rows="2"
              placeholder="请输入需求描述"
            />
          </el-form-item>
        </el-form>

        <div class="dual-ownership-warning">
          <div class="wow-title">⚠ 此子需求将同时归属：</div>
          <ul>
            <li>原需求「{{ parentRequirementName || '（请选择父需求）' }}」</li>
            <li>当前业务编排「{{ designData.name || designData.code || '当前编排' }}」</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="showSubReqDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCreateSubRequirement">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.designer-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  gap: 12px;

  .top-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: white;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .title-input {
      width: 260px;
      :deep(.el-input__wrapper) {
        box-shadow: none;
        border: 1px solid #e4e7ed;
      }
    }

    .status-select {
      width: 120px;
    }
  }

  .designer-main {
    flex: 1;
    display: flex;
    gap: 12px;
    overflow: hidden;
  }
}

.left-panel {
  width: 280px;
  background: white;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-y: auto;
  flex-shrink: 0;

  .panel-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .panel-section-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .node-type-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .node-type-card {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border: 2px solid;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    background: #fafbfc;

    &:hover {
      background: #f0f7ff;
      transform: translateX(2px);
    }

    .ntc-icon {
      width: 32px;
      height: 32px;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 16px;
      font-weight: bold;
    }

    .ntc-info {
      flex: 1;

      .ntc-label {
        font-weight: 600;
        font-size: 14px;
        margin-bottom: 2px;
      }

      .ntc-desc {
        font-size: 12px;
        color: #909399;
      }
    }

    .ntc-add {
      color: #c0c4cc;
    }
  }

  .method-library {
    .library-tip {
      font-size: 11px;
      color: #909399;
      background: #f4f4f5;
      padding: 4px 8px;
      border-radius: 4px;
      margin-bottom: 8px;
      text-align: center;
    }

    .library-methods {
      max-height: 300px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .library-method-item {
      padding: 8px 10px;
      border: 1px solid #e4e7ed;
      border-radius: 6px;
      cursor: grab;
      transition: all 0.2s;
      display: flex;
      align-items: flex-start;
      gap: 8px;

      &:hover {
        border-color: #409eff;
        background: #f0f7ff;
      }

      &.selected {
        border-color: #409eff;
        background: #ecf5ff;
      }

      &.dragging {
        opacity: 0.5;
        cursor: grabbing;
      }

      .lm-drag-handle {
        color: #c0c4cc;
        font-size: 12px;
        cursor: grab;
        flex-shrink: 0;
        margin-top: 2px;
      }

      .lm-content {
        flex: 1;
        min-width: 0;
      }

      .lm-name {
        font-weight: 500;
        font-size: 13px;
      }

      .lm-meta {
        font-size: 11px;
        color: #909399;
        margin-top: 2px;
        display: flex;
        gap: 6px;
      }

      .lm-code {
        font-family: monospace;
      }
    }

    .library-empty {
      text-align: center;
      color: #c0c4cc;
      font-size: 13px;
      padding: 20px 0;
    }
  }
}

.center-canvas {
  flex: 1;
  background: white;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-y: auto;
  min-width: 0;

  .empty-canvas {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
  }

  .canvas-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
  }

  .canvas-connector {
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 36px;
    position: relative;

    .connector-arrow {
      font-size: 18px;
      font-weight: bold;
      z-index: 1;
      background: white;
      padding: 0 4px;
    }

    .connector-line {
      width: 2px;
      height: 100%;
      min-height: 8px;
      position: absolute;
      top: 0;
      left: 50%;
      transform: translateX(-50%);
    }
  }

  .canvas-node {
    width: 420px;
    border: 2px solid;
    border-radius: 10px;
    background: #fafbfc;
    transition: all 0.2s;
    cursor: pointer;
    overflow: hidden;

    &.selected {
      box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
      transform: scale(1.01);
    }

    &.drag-over {
      box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.5);
      border-style: dashed !important;
      background: #f0f9eb;
    }

    .node-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 14px;
      border-bottom: 1px solid;
      background: white;

      .node-header-left {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .node-type-tag {
        color: white;
        padding: 2px 10px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
      }

      .node-name {
        font-weight: 600;
        font-size: 15px;
      }
    }

    .node-content {
      padding: 14px;
    }

    .node-desc {
      color: #909399;
      font-size: 13px;
      margin-bottom: 8px;
    }

    .node-loop {
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
      gap: 6px;
    }

    .node-empty-methods {
      text-align: center;
      color: #c0c4cc;
      font-size: 13px;
      padding: 8px;
    }

    .method-row {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 10px;
      background: white;
      border-radius: 6px;
      border: 1px solid #ebeef5;
      cursor: default;
      transition: all 0.15s;

      &[draggable="true"] {
        cursor: grab;

        &:hover {
          border-color: #c0c4cc;
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
        }

        &.dragging-row {
          opacity: 0.5;
          cursor: grabbing;
        }

        &.method-drag-over {
          border-top: 2px solid #409eff;
        }
      }

      .mr-drag-handle {
        color: #c0c4cc;
        font-size: 12px;
        cursor: grab;
        flex-shrink: 0;
        transition: color 0.2s;

        &:hover {
          color: #909399;
        }
      }

      .method-order {
        color: #909399;
        font-size: 13px;
      }

      .mr-name {
        flex: 1;
        font-weight: 500;
        font-size: 14px;
      }
    }

    .parallel-branch {
      position: relative;
      padding-left: 16px;
      margin: 4px 0;

      .branch-line {
        position: absolute;
        left: 4px;
        top: 0;
        bottom: 0;
        width: 2px;
        background: #dcdfe6;
      }
    }

    .method-branch-item {
      position: relative;
      padding-left: 12px;
      margin-bottom: 6px;
      cursor: default;

      &[draggable="true"] {
        cursor: grab;

        &:hover .branch-method {
          border-color: #c0c4cc;
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
        }

        &.method-drag-over .branch-method {
          border-top: 2px solid #409eff;
        }
      }

      .branch-line {
        position: absolute;
        left: -12px;
        top: 14px;
        width: 12px;
        height: 2px;
        background: #dcdfe6;
      }

      .branch-method {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 10px;
        background: white;
        border-radius: 6px;
        border: 1px solid #ebeef5;
        transition: all 0.15s;

        .bm-drag-handle {
          color: #c0c4cc;
          font-size: 12px;
          cursor: grab;
          flex-shrink: 0;
          transition: color 0.2s;

          &:hover {
            color: #909399;
          }
        }

        .bm-name {
          flex: 1;
          font-weight: 500;
          font-size: 14px;
        }
      }
    }

    .node-footer {
      margin-top: 10px;
      text-align: center;
      display: flex;
      justify-content: center;
      gap: 12px;
    }
  }
}

.right-panel {
  width: 320px;
  background: white;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-y: auto;
  flex-shrink: 0;

  &.right-panel-empty {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .panel-section {
    .panel-section-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 14px;
    }
  }

  .property-form {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .property-item {
    label {
      display: block;
      font-size: 13px;
      color: #606266;
      margin-bottom: 6px;
      font-weight: 500;
    }
  }

  .order-actions {
    display: flex;
    gap: 8px;
  }

  .property-method-list {
    max-height: 200px;
    overflow-y: auto;
    border: 1px solid #ebeef5;
    border-radius: 6px;
    padding: 6px;
    background: #fafbfc;

    .pm-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 8px;
      background: white;
      border-radius: 4px;
      margin-bottom: 4px;

      .pm-order {
        width: 20px;
        height: 20px;
        background: #ecf5ff;
        color: #409eff;
        border-radius: 50%;
        font-size: 11px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .pm-name {
        flex: 1;
        font-size: 13px;
        font-weight: 500;
      }

      &:last-child {
        margin-bottom: 0;
      }
    }

    .pm-empty {
      text-align: center;
      color: #c0c4cc;
      font-size: 13px;
      padding: 12px;
    }
  }
}

.method-dialog {
  .method-dialog-list {
    max-height: 350px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .method-dialog-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #409eff;
      background: #f0f7ff;
    }

    &.selected {
      border-color: #409eff;
      background: #ecf5ff;
    }

    .mdi-main {
      flex: 1;
    }

    .mdi-name {
      font-weight: 600;
      font-size: 14px;
      margin-bottom: 4px;
    }

    .mdi-meta {
      display: flex;
      gap: 8px;
      font-size: 12px;
      color: #909399;

      .mdi-code {
        font-family: monospace;
      }
    }

    .mdi-check {
      color: #409eff;
    }
  }

  .method-dialog-empty {
    text-align: center;
    color: #c0c4cc;
    padding: 30px 0;
  }

  .sub-req-section {
    .sub-req-title {
      font-weight: 600;
      font-size: 14px;
      margin-bottom: 10px;
      color: #303133;
    }
  }
}

.sub-req-dialog {
  .req-info-row {
    background: #f4f4f5;
    padding: 10px 12px;
    border-radius: 6px;
    margin-bottom: 14px;

    .ri-label {
      color: #606266;
      font-size: 13px;
    }

    .ri-value {
      font-weight: 600;
      color: #303133;
    }

    .ri-model {
      color: #909399;
      font-size: 13px;
      margin-left: 4px;
    }
  }

  .dual-ownership-warning {
    background: #fdf6ec;
    border: 1px solid #faecd8;
    border-radius: 6px;
    padding: 12px 14px;
    margin-top: 14px;

    .wow-title {
      font-weight: 600;
      color: #e6a23c;
      margin-bottom: 6px;
      font-size: 13px;
    }

    ul {
      margin: 0;
      padding-left: 20px;
      font-size: 13px;
      color: #606266;

      li {
        margin-bottom: 2px;
      }
    }
  }
}
</style>
