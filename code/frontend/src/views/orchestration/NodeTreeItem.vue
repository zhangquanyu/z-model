<script setup lang="ts">
import { ref } from 'vue'
import type { OrchestrationNode } from '@/api/orchestration'

const props = defineProps<{
  node: OrchestrationNode
  nodeTypeMap: Record<string, { label: string; color: string }>
  index?: number
  level?: number
}>()

const expanded = ref(true)
const hasChildren = () => props.node.children && props.node.children.length > 0

const toggleExpand = () => {
  if (hasChildren()) {
    expanded.value = !expanded.value
  }
}
</script>

<template>
  <div class="node-tree-item" :style="{ marginLeft: (level || 0) * 20 + 'px' }">
    <div class="node-card">
      <div class="node-header">
        <span 
          v-if="hasChildren()" 
          class="expand-toggle"
          :class="{ expanded }"
          @click="toggleExpand"
        >▶</span>
        <span v-else class="expand-placeholder"></span>
        <span class="node-index" v-if="index !== undefined">{{ index + 1 }}</span>
        <el-tag :style="{ backgroundColor: nodeTypeMap[node.nodeType]?.color, color: '#fff', border: 'none' }">
          {{ nodeTypeMap[node.nodeType]?.label || node.nodeType }}
        </el-tag>
        <span class="node-name">{{ node.nodeName || '未命名节点' }}</span>
        <el-tag v-if="node.nodeType === 'LOOP'" type="warning" size="small">
          循环{{ node.loopCount || 1 }}次
        </el-tag>
      </div>
      <div class="node-description" v-if="node.description">
        {{ node.description }}
      </div>
      <div class="node-methods">
        <div v-if="!node.methods || node.methods.length === 0" class="no-methods">
          未绑定方法
        </div>
        <div v-else class="methods-list">
          <div v-for="(method, mIdx) in node.methods" :key="method.id || mIdx" class="method-item">
            <div class="method-info">
              <span class="method-order" v-if="node.nodeType === 'SERIAL'">{{ mIdx + 1 }}.</span>
              <span class="method-name">{{ method.methodName || '未知方法' }}</span>
            </div>
            <div class="method-meta">
              <el-tag v-if="method.modelName" size="small" type="info">{{ method.modelName }}</el-tag>
              <el-tag v-if="method.requirementName" size="small" type="success">{{ method.requirementName }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 递归渲染子节点 -->
    <div v-if="hasChildren() && expanded" class="node-children">
      <NodeTreeItem
        v-for="(child, childIdx) in node.children"
        :key="child.id || childIdx"
        :node="child"
        :node-type-map="nodeTypeMap"
        :index="childIdx"
        :level="(level || 0) + 1"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.node-tree-item {
  margin-bottom: 10px;

  .node-card {
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 12px 14px;
    background: #fafbfc;
    border-left: 3px solid var(--node-color, #409EFF);
  }

  .node-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
    flex-wrap: wrap;
  }

  .expand-toggle {
    cursor: pointer;
    font-size: 12px;
    color: #909399;
    transition: transform 0.2s;
    user-select: none;

    &.expanded {
      transform: rotate(90deg);
    }
  }

  .expand-placeholder {
    display: inline-block;
    width: 12px;
  }

  .node-index {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: var(--primary-color, #409EFF);
    color: white;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
  }

  .node-name {
    font-weight: 500;
    font-size: 14px;
  }

  .node-description {
    color: #909399;
    font-size: 12px;
    margin-bottom: 6px;
    padding-left: 20px;
  }

  .node-methods {
    padding-left: 20px;
  }

  .no-methods {
    color: #c0c4cc;
    font-size: 12px;
    font-style: italic;
  }

  .methods-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .method-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 10px;
    background: white;
    border-radius: 4px;
    border: 1px solid #ebeef5;

    .method-info {
      display: flex;
      align-items: center;
      gap: 6px;

      .method-order {
        color: #909399;
        font-size: 12px;
      }

      .method-name {
        font-weight: 500;
        font-size: 13px;
      }
    }

    .method-meta {
      display: flex;
      gap: 4px;
    }
  }

  .node-children {
    margin-top: 10px;
    padding-left: 16px;
    border-left: 2px dashed #dcdfe6;
  }
}
</style>
