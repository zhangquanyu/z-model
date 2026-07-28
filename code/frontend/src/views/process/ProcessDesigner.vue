<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElButton, ElMessageBox } from 'element-plus'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import { processApi, type NodeModelBinding, type BpmnProcessVersion } from '@/api/process'
import { modelApi, type Model } from '@/api/model'

const route = useRoute()
const processId = route.params.id as string

const canvasRef = ref<HTMLElement | null>(null)
let modeler: BpmnModeler | null = null

const processName = ref('')
const processCode = ref('')
const processDescription = ref('')
const loading = ref(false)
const saving = ref(false)

const selectedElement = ref<any>(null)
const selectedElementId = ref('')
const selectedElementType = ref('')

const nodeBindings = ref<NodeModelBinding[]>([])
const versions = ref<BpmnProcessVersion[]>([])

const showBindDialog = ref(false)
const availableModels = ref<Model[]>([])
const selectedModelId = ref('')

const showVersionDialog = ref(false)

const loadProcess = async () => {
  loading.value = true
  try {
    const res = await processApi.getById(processId)
    processName.value = res.name
    processCode.value = res.code
    processDescription.value = res.description || ''
    
    if (modeler && res.bpmnXml) {
      await modeler.importXML(res.bpmnXml)
    }
    
    nodeBindings.value = res.nodeBindings || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadVersions = async () => {
  try {
    versions.value = await processApi.getVersions(processId)
  } catch (e) {
    console.error('加载版本失败', e)
  }
}

const loadModels = async () => {
  try {
    const res = await modelApi.list({ page: 0, size: 1000 })
    availableModels.value = res.content || []
  } catch (e) {
    console.error('加载模型失败', e)
  }
}

const initModeler = () => {
  if (!canvasRef.value) return
  
  const newModeler = new BpmnModeler({
    container: canvasRef.value
  }) as any
  
  modeler = newModeler
  
  newModeler.on('element.click', (event: any) => {
    const element = event.element
    selectedElement.value = element
    selectedElementId.value = element.id
    selectedElementType.value = element.type
    
    if (element.type === 'bpmn:Task' || element.type === 'bpmn:UserTask' || 
        element.type === 'bpmn:ServiceTask' || element.type === 'bpmn:SubProcess') {
      const bindings = nodeBindings.value.filter(b => b.nodeId === element.id)
      selectedNodeBindings.value = bindings
    } else {
      selectedNodeBindings.value = []
    }
  })
  
  newModeler.on('create.end', () => {
    updateBpmnXml()
  })
  
  newModeler.on('shape.move.end', () => {
    updateBpmnXml()
  })
  
  newModeler.on('element.updateProperties', () => {
    updateBpmnXml()
  })
  
  newModeler.createDiagram()
}

const selectedNodeBindings = ref<NodeModelBinding[]>([])

const updateBpmnXml = async () => {
  if (!modeler) return
  const result = await (modeler as any).saveXML({ format: true })
  currentBpmnXml.value = result.xml
}

const currentBpmnXml = ref('')

const handleSave = async () => {
  if (!modeler) return
  
  saving.value = true
  try {
    const result = await (modeler as any).saveXML({ format: true })
    const xml = result.xml
    
    await processApi.update(processId, {
      bpmnXml: xml,
      changeNote: '手动保存'
    })
    ElMessage.success('保存成功')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleSaveInfo = async () => {
  try {
    await processApi.update(processId, {
      name: processName.value,
      description: processDescription.value
    })
    ElMessage.success('保存成功')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  }
}

const handleExport = async () => {
  if (!modeler) return
  const result = await (modeler as any).saveXML({ format: true })
  const xml = result.xml || ''
  const blob = new Blob([xml], { type: 'application/xml' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${processName.value || 'process'}.bpmn`
  a.click()
  URL.revokeObjectURL(url)
}

const handleZoomIn = () => {
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom(canvas.zoom() * 1.2)
}

const handleZoomOut = () => {
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom(canvas.zoom() / 1.2)
}

const handleFitView = () => {
  if (!modeler) return
  const canvas = (modeler as any).get('canvas')
  canvas.zoom('fit-viewport')
}

const handleReset = () => {
  if (!modeler) return
  ;(modeler as any).createDiagram()
  nodeBindings.value = []
  ElMessage.info('已重置流程图')
}

const openBindDialog = async () => {
  if (!selectedElementId.value) {
    ElMessage.warning('请先选择一个任务节点')
    return
  }
  if (!['bpmn:Task', 'bpmn:UserTask', 'bpmn:ServiceTask', 'bpmn:SubProcess'].includes(selectedElementType.value)) {
    ElMessage.warning('只能对任务类型节点绑定模型')
    return
  }
  showBindDialog.value = true
  selectedModelId.value = ''
  await loadModels()
}

const handleBind = async () => {
  if (!selectedModelId.value) {
    ElMessage.warning('请选择模型')
    return
  }
  try {
    const binding = await processApi.bindNodeModel(processId, {
      nodeId: selectedElementId.value,
      modelId: selectedModelId.value
    })
    nodeBindings.value.push(binding)
    selectedNodeBindings.value.push(binding)
    ElMessage.success('绑定成功')
    showBindDialog.value = false
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '绑定失败')
  }
}

const handleUnbind = async (binding: NodeModelBinding) => {
  try {
    await processApi.unbindNodeModel(processId, binding.nodeId)
    nodeBindings.value = nodeBindings.value.filter(b => b.id !== binding.id)
    selectedNodeBindings.value = selectedNodeBindings.value.filter(b => b.id !== binding.id)
    ElMessage.success('解绑成功')
  } catch (e) {
    ElMessage.error('解绑失败')
  }
}

const openVersionDialog = async () => {
  showVersionDialog.value = true
  await loadVersions()
}

const handleRollback = async (version: number) => {
  try {
    await ElMessageBox.confirm(`确定要回滚到版本 ${version} 吗？`, '回滚确认', {
      type: 'warning'
    })
    const res = await processApi.rollbackVersion(processId, version)
    if (modeler && res.bpmnXml) {
      await (modeler as any).importXML(res.bpmnXml)
    }
    ElMessage.success('回滚成功')
    showVersionDialog.value = false
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '回滚失败')
    }
  }
}

onMounted(async () => {
  initModeler()
  await loadProcess()
})

onBeforeUnmount(() => {
  if (modeler) {
    modeler.destroy()
    modeler = null
  }
})

watch([() => route.params.id], async () => {
  if (modeler) {
    await loadProcess()
  }
})
</script>

<template>
  <div class="process-designer" v-loading="loading">
    <div class="designer-header">
      <div class="header-info">
        <el-input
          v-model="processName"
          placeholder="流程名称"
          style="width: 250px"
          @blur="handleSaveInfo"
        />
        <span class="code">{{ processCode }}</span>
      </div>
      <div class="header-actions">
        <el-button @click="handleZoomOut">缩小</el-button>
        <el-button @click="handleZoomIn">放大</el-button>
        <el-button @click="handleFitView">适应窗口</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="openVersionDialog">版本历史</el-button>
        <el-button type="primary" @click="handleExport">导出BPMN</el-button>
        <el-button type="success" @click="handleSave" :loading="saving">保存</el-button>
      </div>
    </div>

    <div class="designer-body">
      <div class="canvas-container" ref="canvasRef"></div>
      
      <div class="side-panel">
        <div class="panel-section">
          <h3>节点属性</h3>
          <div v-if="selectedElement">
            <p><strong>ID:</strong> {{ selectedElementId }}</p>
            <p><strong>类型:</strong> {{ selectedElementType }}</p>
          </div>
          <p v-else class="hint">点击画布中的元素查看属性</p>
        </div>
        
        <div class="panel-section">
          <h3>模型绑定</h3>
          <div v-if="selectedNodeBindings.length > 0">
            <div v-for="binding in selectedNodeBindings" :key="binding.id" class="binding-item">
              <span>{{ binding.modelName }} ({{ binding.modelCode }})</span>
              <el-button size="small" type="danger" link @click="handleUnbind(binding)">解绑</el-button>
            </div>
          </div>
          <p v-else class="hint">当前节点未绑定模型</p>
          <el-button 
            type="primary" 
            size="small" 
            :disabled="!selectedElementId || !['bpmn:Task', 'bpmn:UserTask', 'bpmn:ServiceTask', 'bpmn:SubProcess'].includes(selectedElementType)"
            @click="openBindDialog"
          >
            绑定模型
          </el-button>
        </div>

        <div class="panel-section">
          <h3>当前流程所有绑定</h3>
          <div v-if="nodeBindings.length > 0">
            <div v-for="binding in nodeBindings" :key="binding.id" class="binding-item">
              <span>{{ binding.nodeId }} → {{ binding.modelName }}</span>
            </div>
          </div>
          <p v-else class="hint">暂无绑定</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="showBindDialog" title="绑定模型到节点" width="500px">
      <el-form label-width="100px">
        <el-form-item label="节点">
          <el-input :model-value="selectedElementId" disabled />
        </el-form-item>
        <el-form-item label="选择模型">
          <el-select v-model="selectedModelId" placeholder="请选择模型" style="width: 100%">
            <el-option 
              v-for="model in availableModels" 
              :key="model.id" 
              :label="`${model.name} (${model.code})`" 
              :value="model.id!"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBind">确定绑定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showVersionDialog" title="版本历史" width="600px">
      <div v-if="versions.length > 0">
        <div v-for="ver in versions" :key="ver.id" class="version-item">
          <div class="version-info">
            <span class="version-num">v{{ ver.version }}</span>
            <span class="version-date">{{ ver.createdAt ? new Date(ver.createdAt).toLocaleString('zh-CN') : '' }}</span>
            <span v-if="ver.changeNote" class="version-note">{{ ver.changeNote }}</span>
          </div>
          <el-button type="primary" size="small" @click="handleRollback(ver.version)">回滚</el-button>
        </div>
      </div>
      <p v-else>暂无版本记录</p>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.process-designer {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  overflow: hidden;

  .designer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    border-bottom: 1px solid #e4e7ed;
    background: #fafafa;

    .header-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .code {
        color: #909399;
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .designer-body {
    flex: 1;
    display: flex;
    overflow: hidden;

    .canvas-container {
      flex: 1;
      overflow: hidden;
      position: relative;
      
      :deep(.bpmn-container) {
        height: 100%;
      }
    }

    .side-panel {
      width: 280px;
      border-left: 1px solid #e4e7ed;
      padding: 16px;
      overflow-y: auto;
      background: #fafafa;

      .panel-section {
        margin-bottom: 20px;

        h3 {
          font-size: 14px;
          color: #303133;
          margin-bottom: 10px;
          padding-bottom: 8px;
          border-bottom: 1px solid #e4e7ed;
        }

        .hint {
          color: #909399;
          font-size: 12px;
          margin: 8px 0;
        }

        .binding-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px;
          background: white;
          border-radius: 4px;
          margin-bottom: 6px;
          font-size: 13px;
        }
      }
    }
  }
}

.version-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;

  .version-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .version-num {
      font-weight: bold;
      color: var(--primary-color);
    }

    .version-date {
      color: #909399;
      font-size: 13px;
    }

    .version-note {
      color: #606266;
      font-size: 13px;
    }
  }
}
</style>
