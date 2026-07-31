<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { functionalOrchestrationApi, type FunctionalOrchestration, type FoNodeDTO } from '@/api/functional-orchestration'
import { physicalModelApi, type PhysicalModel } from '@/api/physical-model'
import { physicalMethodApi, type PhysicalMethod } from '@/api/physical-model'
import { ArrowLeft, Document, DocumentChecked, Plus, Delete, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string

const orchestration = ref<FunctionalOrchestration | null>(null)
const physicalModels = ref<PhysicalModel[]>([])
const physicalMethods = ref<PhysicalMethod[]>([])
const nodes = ref<FoNodeDTO[]>([])
const draggedNode = ref<string | null>(null)
const showNodeConfig = ref(false)
const currentNode = ref<FoNodeDTO | null>(null)
const canvasRef = ref<HTMLElement | null>(null)

const nodeTypes = [
  { type: 'DB_READ', label: '数据库读取', color: '#2196f3', icon: '📖' },
  { type: 'DB_WRITE', label: '数据库写入', color: '#4caf50', icon: '✏️' },
  { type: 'API_CALL', label: 'API调用', color: '#ff9800', icon: '🌐' },
  { type: 'TRANSFORM', label: '数据转换', color: '#9c27b0', icon: '🔄' },
  { type: 'CONDITION', label: '条件判断', color: '#fbc02d', icon: '❓' },
  { type: 'CUSTOM', label: '自定义', color: '#c2185b', icon: '⚙️' }
]

const loadData = async () => {
  try {
    orchestration.value = await functionalOrchestrationApi.getById(id)
    nodes.value = orchestration.value.nodes || []
  } catch (error) {
    console.error('Failed to load orchestration:', error)
  }
}

const loadPhysicalModels = async () => {
  try {
    physicalModels.value = await physicalModelApi.listAll()
  } catch (error) {
    console.error('Failed to load physical models:', error)
  }
}

const loadPhysicalMethods = async () => {
  try {
    // 如果有选中的物理模型，加载其方法
    if (currentNode.value?.physicalModelId) {
      physicalMethods.value = await physicalMethodApi.listByPhysicalModelId(currentNode.value.physicalModelId)
    }
  } catch (error) {
    console.error('Failed to load physical methods:', error)
  }
}

const handleGoBack = () => {
  router.push(`/functional-orchestrations/${id}`)
}

const handleAddNode = (type: string) => {
  const newNode: FoNodeDTO = {
    id: `node_${Date.now()}`,
    nodeType: type,
    label: getNodeTypeLabel(type),
    sortOrder: nodes.value.length,
    x: 100 + nodes.value.length * 30,
    y: 100 + nodes.value.length * 30,
    methods: [],
    nodeConfig: {}
  }
  nodes.value.push(newNode)
}

const handleNodeClick = (node: FoNodeDTO) => {
  currentNode.value = node
  showNodeConfig.value = true
  if (node.physicalModelId) {
    loadPhysicalMethods()
  }
}

const handleNodeDelete = async (nodeId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个节点吗？', '提示', {
      type: 'warning'
    })
    nodes.value = nodes.value.filter(n => n.id !== nodeId)
    ElMessage.success('删除成功')
  } catch (error) {
    // 用户取消
  }
}

const handleNodeDragStart = (nodeId: string) => {
  draggedNode.value = nodeId
}

const handleNodeDragEnd = () => {
  draggedNode.value = null
}

const handleCanvasDrop = (e: DragEvent) => {
  e.preventDefault()
  if (!draggedNode.value || !canvasRef.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  const node = nodes.value.find(n => n.id === draggedNode.value)
  if (node) {
    node.x = x
    node.y = y
  }

  draggedNode.value = null
}

const handleNodeMove = (e: MouseEvent, node: FoNodeDTO) => {
  e.preventDefault()
  if (!canvasRef.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const startX = e.clientX
  const startY = e.clientY
  const startNodeX = node.x || 0
  const startNodeY = node.y || 0

  const handleMouseMove = (moveEvent: MouseEvent) => {
    node.x = startNodeX + (moveEvent.clientX - startX)
    node.y = startNodeY + (moveEvent.clientY - startY)
  }

  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const handleSave = async () => {
  try {
    await functionalOrchestrationApi.saveNodes(id, nodes.value)
    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
    console.error('Failed to save:', error)
  }
}

const handleGenerateCode = async () => {
  await handleSave()
  router.push(`/functional-orchestrations/${id}`)
}

const handleConfigSave = () => {
  showNodeConfig.value = false
  ElMessage.success('配置已更新')
}

const handlePhysicalModelChange = (physicalModelId: string) => {
  if (currentNode.value) {
    currentNode.value.physicalModelId = physicalModelId
    currentNode.value.methods = []
    loadPhysicalMethods()
  }
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

const getNodeTypeInfo = (type: string) => {
  return nodeTypes.find(t => t.type === type) || nodeTypes[0]
}

onMounted(() => {
  loadData()
  loadPhysicalModels()
})
</script>

<template>
  <div class="fo-designer">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <button class="back-btn" @click="handleGoBack">
          <ArrowLeft />
          返回
        </button>
        <h2 class="page-title">功能编排设计 - {{ orchestration?.name }}</h2>
      </div>
      <div class="toolbar-right">
        <button class="action-btn generate" @click="handleGenerateCode">
          <Document />
          保存并生成代码
        </button>
        <button class="action-btn save" @click="handleSave">
          <DocumentChecked />
          保存
        </button>
      </div>
    </div>

    <div class="designer-container">
      <!-- 左侧节点面板 -->
      <div class="node-palette">
        <h3 class="palette-title">节点类型</h3>
        <div class="node-types">
          <div
            v-for="nodeType in nodeTypes"
            :key="nodeType.type"
            :class="['node-type-item', nodeType.type.toLowerCase()]"
            @click="handleAddNode(nodeType.type)"
          >
            <span class="node-icon">{{ nodeType.icon }}</span>
            <span class="node-label">{{ nodeType.label }}</span>
          </div>
        </div>

        <div class="palette-hint">
          <p>点击添加节点，拖拽调整位置</p>
        </div>

        <div class="nodes-list">
          <h3 class="palette-title">已添加节点 ({{ nodes.length }})</h3>
          <div v-for="(node, index) in nodes" :key="node.id" class="node-list-item">
            <span class="node-order">{{ index + 1 }}</span>
            <span class="node-type-label">{{ getNodeTypeInfo(node.nodeType).icon }}</span>
            <span class="node-name">{{ node.label || getNodeTypeLabel(node.nodeType) }}</span>
            <button class="delete-btn" @click="handleNodeDelete(node.id!)">
              <Delete />
            </button>
          </div>
          <div class="empty-nodes" v-if="nodes.length === 0">
            <p>还没有节点</p>
          </div>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div
        ref="canvasRef"
        class="canvas-area"
        @drop="handleCanvasDrop"
        @dragover.prevent
      >
        <div class="canvas-grid">
          <div
            v-for="node in nodes"
            :key="node.id"
            :class="['canvas-node', node.nodeType.toLowerCase()]"
            :style="{
              left: (node.x || 0) + 'px',
              top: (node.y || 0) + 'px'
            }"
            @click="handleNodeClick(node)"
            @mousedown="handleNodeMove($event, node)"
          >
            <div class="node-header">
              <span class="node-icon">{{ getNodeTypeInfo(node.nodeType).icon }}</span>
              <span class="node-type-badge">{{ getNodeTypeLabel(node.nodeType) }}</span>
              <button class="node-delete" @click.stop="handleNodeDelete(node.id!)">
                <Delete />
              </button>
            </div>
            <div class="node-body">
              <span class="node-label">{{ node.label || getNodeTypeLabel(node.nodeType) }}</span>
              <div class="node-meta" v-if="node.methods && node.methods.length > 0">
                <span class="meta-item">{{ node.methods.length }} 个方法</span>
              </div>
              <div class="node-meta" v-if="node.nodeConfig?.configKey">
                <span class="meta-item config">{{ node.nodeConfig.configKey }}: {{ node.nodeConfig.configValue?.slice(0, 20) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="canvas-hint" v-if="nodes.length === 0">
          <p>从左侧点击添加节点，或拖拽节点到画布</p>
        </div>
      </div>
    </div>

    <!-- 节点配置对话框 -->
    <el-dialog 
      v-model="showNodeConfig" 
      :title="'配置 - ' + (currentNode?.label || getNodeTypeLabel(currentNode?.nodeType || ''))" 
      width="600px"
    >
      <el-form :model="currentNode" label-width="120px" v-if="currentNode">
        <el-form-item label="节点类型">
          <el-select v-model="currentNode.nodeType" style="width: 100%" @change="currentNode.label = getNodeTypeLabel(currentNode.nodeType)">
            <el-option v-for="nt in nodeTypes" :key="nt.type" :label="nt.label" :value="nt.type" />
          </el-select>
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input v-model="currentNode.label" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="currentNode.sortOrder" :min="0" />
        </el-form-item>

        <!-- 数据库节点配置 -->
        <template v-if="['DB_READ', 'DB_WRITE'].includes(currentNode.nodeType)">
          <el-form-item label="物理模型">
            <el-select 
              v-model="currentNode.physicalModelId" 
              placeholder="选择物理模型" 
              clearable
              style="width: 100%"
              @change="handlePhysicalModelChange(currentNode.physicalModelId!)"
            >
              <el-option v-for="pm in physicalModels" :key="pm.id" :label="pm.name" :value="pm.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="关联方法">
            <el-select 
              v-model="currentNode.methods" 
              multiple 
              placeholder="选择关联的方法"
              style="width: 100%"
            >
              <el-option 
                v-for="m in physicalMethods" 
                :key="m.id" 
                :label="`${m.name} (${m.methodType})`" 
                :value="{ id: m.id, methodId: m.id, physicalModelId: currentNode.physicalModelId, sortOrder: 0 }"
              />
            </el-select>
          </el-form-item>
        </template>

        <!-- API 调用节点配置 -->
        <template v-if="currentNode.nodeType === 'API_CALL'">
          <el-form-item label="API URL">
            <el-input v-model="currentNode.nodeConfig!.configValue" placeholder="https://api.example.com" />
          </el-form-item>
          <el-form-item label="配置键">
            <el-input v-model="currentNode.nodeConfig!.configKey" placeholder="api_url" />
          </el-form-item>
        </template>

        <!-- 条件判断节点配置 -->
        <template v-if="currentNode.nodeType === 'CONDITION'">
          <el-form-item label="条件表达式">
            <el-input 
              v-model="currentNode.nodeConfig!.configValue" 
              type="textarea" 
              :rows="2"
              placeholder="例如: result.status === 'success'"
            />
          </el-form-item>
          <el-form-item label="配置键">
            <el-input v-model="currentNode.nodeConfig!.configKey" placeholder="condition_expression" />
          </el-form-item>
        </template>

        <!-- 数据转换节点配置 -->
        <template v-if="currentNode.nodeType === 'TRANSFORM'">
          <el-form-item label="转换描述">
            <el-input 
              v-model="currentNode.nodeConfig!.configValue" 
              type="textarea" 
              :rows="2"
              placeholder="描述数据转换逻辑"
            />
          </el-form-item>
        </template>

        <!-- 自定义节点配置 -->
        <template v-if="currentNode.nodeType === 'CUSTOM'">
          <el-form-item label="自定义配置">
            <el-input 
              v-model="currentNode.nodeConfig!.configValue" 
              type="textarea" 
              :rows="3"
              placeholder="自定义节点配置"
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showNodeConfig = false">取消</el-button>
        <el-button type="primary" @click="handleConfigSave">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.fo-designer {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
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
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.toolbar-right {
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

  &.generate {
    background-color: #f3e5f5;
    color: #9c27b0;

    &:hover {
      background-color: #e1bee7;
    }
  }

  &.save {
    background-color: #1e3a5f;
    color: white;

    &:hover {
      background-color: #2d4a6f;
    }
  }
}

.designer-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.node-palette {
  width: 280px;
  background-color: white;
  border-right: 1px solid #e0e0e0;
  padding: 16px;
  overflow-y: auto;
}

.palette-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.node-types {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.node-type-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background-color: #e8ebf0;
    transform: translateX(4px);
  }

  &.db-read {
    border-left: 3px solid #2196f3;
  }

  &.db-write {
    border-left: 3px solid #4caf50;
  }

  &.api-call {
    border-left: 3px solid #ff9800;
  }

  &.transform {
    border-left: 3px solid #9c27b0;
  }

  &.condition {
    border-left: 3px solid #fbc02d;
  }

  &.custom {
    border-left: 3px solid #c2185b;
  }
}

.node-icon {
  font-size: 20px;
}

.node-label {
  font-size: 14px;
  color: #333;
}

.palette-hint {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 24px;

  p {
    font-size: 12px;
    color: #666;
    margin: 0;
  }
}

.nodes-list {
  h3 {
    margin-top: 24px;
  }
}

.node-list-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #fafbfc;
  border-radius: 6px;
  margin-bottom: 4px;
}

.node-order {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #1e3a5f;
  color: white;
  border-radius: 50%;
  font-size: 12px;
}

.node-type-label {
  font-size: 16px;
}

.node-name {
  flex: 1;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-btn {
  padding: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  border-radius: 4px;

  &:hover {
    background-color: #ffebee;
    color: #f44336;
  }

  svg {
    width: 12px;
    height: 12px;
  }
}

.empty-nodes {
  text-align: center;
  padding: 16px;
  color: #999;
  font-size: 12px;
}

.canvas-area {
  flex: 1;
  position: relative;
  overflow: auto;
  background-color: #fafbfc;
  background-image: radial-gradient(circle, #e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;
  min-height: 500px;
  min-width: 500px;
}

.canvas-grid {
  position: relative;
  width: 2000px;
  height: 2000px;
}

.canvas-node {
  position: absolute;
  min-width: 180px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  cursor: move;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  }

  &.db-read {
    border-top: 4px solid #2196f3;
  }

  &.db-write {
    border-top: 4px solid #4caf50;
  }

  &.api-call {
    border-top: 4px solid #ff9800;
  }

  &.transform {
    border-top: 4px solid #9c27b0;
  }

  &.condition {
    border-top: 4px solid #fbc02d;
  }

  &.custom {
    border-top: 4px solid #c2185b;
  }
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.node-icon {
  font-size: 18px;
}

.node-type-badge {
  flex: 1;
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.node-delete {
  padding: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: #ccc;
  border-radius: 4px;

  &:hover {
    color: #f44336;
  }

  svg {
    width: 12px;
    height: 12px;
  }
}

.node-body {
  padding: 12px 16px;
}

.node-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.node-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-item {
  padding: 4px 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #666;

  &.config {
    background-color: #fff3e0;
    color: #ff9800;
  }
}

.canvas-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #999;
  font-size: 14px;

  p {
    padding: 24px 32px;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
}
</style>
