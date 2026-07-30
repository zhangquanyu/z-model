<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'

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
  parentId?: string
  nodeName: string
  description?: string
  nodeType: string
  sortOrder: number
  loopCount?: number
  width?: number
  methods: DesignMethod[]
  children?: DesignNode[]
}

const props = defineProps<{
  node: DesignNode
  nodeTypeMap: Record<string, { label: string; color: string; icon: string; desc: string }>
  selectedNodeId: string | null
  draggingNodeMethod: { nodeId: string; methodId: string } | null
  dragOverMethodIndex: { nodeId: string; index: number } | null
  draggingNodeId: string | null
  dragOverNodeIdForSort: string | null
  dragOverPosition: 'before' | 'after'
  canvasLayout: 'vertical' | 'horizontal'
  draggingMethod: { id?: string; name?: string; modelId?: string; modelName?: string; requirementId?: string; requirementName?: string } | null
  onSelectNode: (id: string) => void
  onDeleteNode: (id: string) => void
  onAddChildNode: (parentId: string, nodeType: string) => void
  onAddMethod: (nodeId: string) => void
  onRemoveMethod: (nodeId: string, methodId: string) => void
  onMethodDragStart: (nodeId: string, methodId: string) => void
  onMethodDragEnd: () => void
  onMethodDragOver: (e: DragEvent, nodeId: string, index: number) => void
  onMethodDragLeave: (nodeId: string, index: number) => void
  onMethodDrop: (e: DragEvent, nodeId: string, index: number) => void
  onNodeDragStart: (nodeId: string) => void
  onNodeDragEnd: () => void
  onNodeDragOver: (e: DragEvent, nodeId: string) => void
  onNodeDrop: (e: DragEvent, nodeId: string) => void
  onDropNode: (e: DragEvent, nodeId: string) => void
}>()

const showAddChildMenu = ref(false)

const addChildNode = (nodeType: string) => {
  if (props.node.id) {
    props.onAddChildNode(props.node.id, nodeType)
  }
  showAddChildMenu.value = false
}

const hasChildren = computed(() => props.node.children && props.node.children.length > 0)

// 并行节点的子节点横向排列，其他节点纵向排列
const childrenLayout = computed(() => {
  return props.node.nodeType === 'PARALLEL' ? 'horizontal' : 'vertical'
})

// 处理 dragover 事件，区分方法拖拽和节点拖拽
const handleDragOver = (e: DragEvent, nodeId: string) => {
  if (props.draggingMethod) {
    e.preventDefault()
    e.stopPropagation()
  } else {
    props.onNodeDragOver(e, nodeId)
  }
}

// 处理 drop 事件，区分方法拖拽和节点拖拽
const handleDrop = (e: DragEvent, nodeId: string) => {
  if (props.draggingMethod) {
    e.preventDefault()
    e.stopPropagation()
    props.onDropNode(e, nodeId)
  } else {
    props.onNodeDrop(e, nodeId)
  }
}

// ============ 节点调整大小 ============
const isResizing = ref(false)
const resizeStartX = ref(0)
const resizeStartWidth = ref(0)
const MIN_WIDTH = 280
const MAX_WIDTH = 800

const currentWidth = computed(() => {
  return props.node.width || 420
})

const startResize = (e: MouseEvent) => {
  e.stopPropagation()
  e.preventDefault()
  isResizing.value = true
  resizeStartX.value = e.clientX
  resizeStartWidth.value = currentWidth.value
  document.addEventListener('mousemove', onResizeMove)
  document.addEventListener('mouseup', onResizeEnd)
}

const onResizeMove = (e: MouseEvent) => {
  if (!isResizing.value) return
  const deltaX = e.clientX - resizeStartX.value
  let newWidth = resizeStartWidth.value + deltaX
  newWidth = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, newWidth))
  props.node.width = newWidth
}

const onResizeEnd = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', onResizeEnd)
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', onResizeEnd)
})
</script>

<template>
  <div
    class="canvas-node"
    :class="{
      selected: selectedNodeId === node.id,
      'node-dragging': draggingNodeId === node.id,
      'node-drag-over': dragOverNodeIdForSort === node.id && draggingNodeId !== node.id,
      'drop-target': !!draggingMethod,
      'layout-horizontal': canvasLayout === 'horizontal',
      'resizing': isResizing
    }"
    :style="{ borderColor: nodeTypeMap[node.nodeType]?.color, width: currentWidth + 'px' }"
    @click.stop="onSelectNode(node.id!)"
    @dragover="(e) => handleDragOver(e, node.id!)"
    @drop="(e) => handleDrop(e, node.id!)"
  >
    <!-- 拖拽指示器 -->
    <div
      v-if="dragOverNodeIdForSort === node.id && draggingNodeId !== node.id"
      class="drag-indicator"
      :class="{ 'indicator-before': dragOverPosition === 'before', 'indicator-after': dragOverPosition === 'after' }"
    ></div>

    <div class="node-header" :style="{ borderBottomColor: nodeTypeMap[node.nodeType]?.color }">
      <div class="node-header-left">
        <span
          class="node-drag-handle"
          :draggable="!!node.id"
          @dragstart="node.id && onNodeDragStart(node.id)"
          @dragend="onNodeDragEnd"
          title="拖拽移动节点"
        >⋮⋮</span>
        <span
          class="node-type-tag"
          :style="{ backgroundColor: nodeTypeMap[node.nodeType]?.color }"
        >
          {{ nodeTypeMap[node.nodeType]?.label }}
        </span>
        <span class="node-name">{{ node.nodeName }}</span>
      </div>
      <div class="node-header-actions" @click.stop>
        <el-dropdown
          trigger="click"
          @command="(cmd: string) => addChildNode(cmd)"
        >
          <el-button link size="small" :icon="Plus" title="添加子节点" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="(info, type) in nodeTypeMap"
                :key="type"
                :command="type"
              >
                <span style="color: inherit;">{{ info.label }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button
          link
          size="small"
          :icon="Delete"
          type="danger"
          @click="onDeleteNode(node.id!)"
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
              @click.stop="onRemoveMethod(node.id!, m.methodId)"
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
          @click.stop="onAddMethod(node.id!)"
        >
          添加方法
        </el-button>
      </div>
    </div>

    <!-- 子节点区域 -->
    <div
      v-if="hasChildren"
      class="node-children"
      :class="{ 'children-horizontal': childrenLayout === 'horizontal' }"
    >
      <!-- 横向布局时的公共连接线 -->
      <div v-if="childrenLayout === 'horizontal'" class="parallel-connector-line"></div>

      <div
        v-for="(child, childIdx) in node.children"
        :key="child.id"
        class="child-node-wrapper"
        :class="{ 'child-horizontal': childrenLayout === 'horizontal' }"
      >
        <div v-if="childIdx > 0 && childrenLayout === 'vertical'" class="child-connector"></div>
        <div v-if="childrenLayout === 'horizontal'" class="child-vertical-line"></div>
        <NodeCard
          :node="child"
          :node-type-map="nodeTypeMap"
          :selected-node-id="selectedNodeId"
          :dragging-node-method="draggingNodeMethod"
          :drag-over-method-index="dragOverMethodIndex"
          :dragging-node-id="draggingNodeId"
          :drag-over-node-id-for-sort="dragOverNodeIdForSort"
          :drag-over-position="dragOverPosition"
          :canvas-layout="canvasLayout"
          :on-select-node="onSelectNode"
          :on-delete-node="onDeleteNode"
          :on-add-child-node="onAddChildNode"
          :on-add-method="onAddMethod"
          :on-remove-method="onRemoveMethod"
          :on-method-drag-start="onMethodDragStart"
          :on-method-drag-end="onMethodDragEnd"
          :on-method-drag-over="onMethodDragOver"
          :on-method-drag-leave="onMethodDragLeave"
          :on-method-drop="onMethodDrop"
          :on-node-drag-start="onNodeDragStart"
          :on-node-drag-end="onNodeDragEnd"
          :on-node-drag-over="onNodeDragOver"
          :on-node-drop="onNodeDrop"
          :dragging-method="draggingMethod"
          :on-drop-node="onDropNode"
        />
      </div>
    </div>

    <!-- 调整大小手柄 -->
    <div
      class="resize-handle"
      @mousedown.stop="startResize"
      title="拖拽调整大小"
    ></div>
  </div>
</template>

<style lang="scss" scoped>
.canvas-node {
  border: 2px solid;
  border-radius: 10px;
  background: #fafbfc;
  transition: all 0.2s;
  cursor: pointer;
  overflow: hidden;
  position: relative;

  &.resizing {
    cursor: col-resize;
    transition: none;
    user-select: none;
  }

  &.selected {
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
    transform: scale(1.01);
  }

  &.node-dragging {
    opacity: 0.5;
    z-index: 1000;
  }

  &.node-drag-over {
    border-style: dashed;
    border-color: #409eff !important;
  }

  &.drop-target {
    cursor: copy;
    transition: all 0.2s;

    &:hover {
      border-color: #67C23A !important;
      box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.3);
      background: #f0f9eb;
    }
  }

  .node-drag-handle {
    color: #c0c4cc;
    cursor: grab;
    font-size: 12px;
    padding: 2px 4px;
    border-radius: 3px;
    transition: all 0.2s;
    user-select: none;

    &:hover {
      color: #409eff;
      background: #ecf5ff;
    }

    &:active {
      cursor: grabbing;
    }
  }

  .drag-indicator {
    position: absolute;
    background: #409eff;
    z-index: 10;

    &.indicator-before {
      top: -3px;
      left: 0;
      right: 0;
      height: 4px;
      border-radius: 2px;
    }

    &.indicator-after {
      bottom: -3px;
      left: 0;
      right: 0;
      height: 4px;
      border-radius: 2px;
    }
  }

  // 横向布局时的指示器样式
  &.layout-horizontal .drag-indicator {
    &.indicator-before {
      top: 0;
      left: -3px;
      right: auto;
      bottom: 0;
      width: 4px;
      height: auto;
    }

    &.indicator-after {
      top: 0;
      left: auto;
      right: -3px;
      bottom: 0;
      width: 4px;
      height: auto;
    }
  }

  // 调整大小手柄
  .resize-handle {
    position: absolute;
    right: 2px;
    bottom: 2px;
    width: 12px;
    height: 12px;
    cursor: col-resize;
    z-index: 20;

    &::before {
      content: '';
      position: absolute;
      right: 2px;
      bottom: 2px;
      width: 8px;
      height: 8px;
      border-right: 2px solid #c0c4cc;
      border-bottom: 2px solid #c0c4cc;
      border-radius: 0 0 2px 0;
      transition: border-color 0.2s;
    }

    &:hover::before {
      border-color: #409eff;
    }
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
    padding: 8px 0 8px 0;
    margin: 4px 0;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 12px;
    padding-left: 0;

    .branch-line {
      display: none;
    }
  }

  .method-branch-item {
    position: relative;
    padding-left: 0;
    margin-bottom: 0;
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
      display: none;
    }

    .branch-method {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: white;
      border-radius: 6px;
      border: 1px solid #d4e4fb;
      border-left: 3px solid #67C23A;
      transition: all 0.15s;

      .bm-drag-handle {
        color: #c0c4cc;
        font-size: 12px;
        cursor: grab;
        flex-shrink: 0;
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
  }

  // 子节点样式 - 纵向（串行/循环）
  .node-children {
    padding: 12px 14px;
    background: #f5f7fa;
    border-top: 1px dashed #e4e7ed;
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 4px;

    // 子节点横向排列（并行节点）
    &.children-horizontal {
      flex-direction: row;
      flex-wrap: wrap;
      padding: 24px 14px 16px;
      border-top: 1px dashed #67C23A;
      background: linear-gradient(180deg, #f0f9eb 0%, #f5f7fa 100%);
      position: relative;
      justify-content: flex-start;

      .parallel-connector-line {
        position: absolute;
        top: 12px;
        left: 14px;
        right: 14px;
        height: 2px;
        background: #67C23A;
        z-index: 1;
      }
    }
  }

  .child-node-wrapper {
    position: relative;
    padding-left: 20px;

    &::before {
      content: '';
      position: absolute;
      left: 8px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: #c0c4cc;
    }

    .child-connector {
      height: 8px;
    }

    // 子节点纵向样式：允许动态宽度
    .canvas-node {
      max-width: 100%;
    }

    // 横向子节点样式
    &.child-horizontal {
      padding-left: 0;
      padding-top: 16px;
      position: relative;

      &::before {
        display: none;
      }

      .child-vertical-line {
        position: absolute;
        top: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 2px;
        height: 16px;
        background: #67C23A;
        z-index: 2;
      }

      // 横向子节点允许动态宽度，但默认稍小
      .canvas-node {
        max-width: 100%;
      }
    }
  }
}
</style>
