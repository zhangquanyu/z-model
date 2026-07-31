<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { physicalPropertyApi, type PhysicalProperty } from '@/api/physical-model'
import { propertyApi } from '@/api/property'
import { ArrowLeft, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const physicalModelId = route.params.id as string

const properties = ref<PhysicalProperty[]>([])
const sourceProperties = ref<any[]>([])
const sourceMethods = ref<any[]>([])
const showCreateDialog = ref(false)
const editingProperty = ref<PhysicalProperty | null>(null)
const creatingProperty = ref<PhysicalProperty>({
  physicalModelId: physicalModelId,
  name: '',
  code: '',
  dataType: 'STRING',
  dbType: '',
  dbLength: 255,
  nullable: true,
  isPrimaryKey: false,
  isIndex: false
})

const loadSourceData = async () => {
  try {
    // 加载可用的源属性和方法
    const res = await propertyApi.list({})
    sourceProperties.value = res.content || []
  } catch (error) {
    console.error('Failed to load source properties:', error)
  }
}

const loadProperties = async () => {
  try {
    properties.value = await physicalPropertyApi.listByPhysicalModelId(physicalModelId)
  } catch (error) {
    console.error('Failed to load physical properties:', error)
  }
}

const handleGoBack = () => {
  router.push(`/physical-models/${physicalModelId}`)
}

const handleCreate = () => {
  creatingProperty.value = {
    physicalModelId: physicalModelId,
    name: '',
    code: '',
    dataType: 'STRING',
    dbType: 'VARCHAR',
    dbLength: 255,
    nullable: true,
    isPrimaryKey: false,
    isIndex: false
  }
  editingProperty.value = null
  showCreateDialog.value = true
}

const handleEdit = (prop: PhysicalProperty) => {
  editingProperty.value = { ...prop }
  creatingProperty.value = { ...prop }
  showCreateDialog.value = true
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个属性吗？', '提示', {
      type: 'warning'
    })
    await physicalPropertyApi.delete(id)
    ElMessage.success('删除成功')
    loadProperties()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete property:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    if (editingProperty.value?.id) {
      await physicalPropertyApi.update(editingProperty.value.id, creatingProperty.value)
      ElMessage.success('更新成功')
    } else {
      await physicalPropertyApi.create(creatingProperty.value)
      ElMessage.success('创建成功')
    }
    showCreateDialog.value = false
    loadProperties()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
    console.error('Failed to save property:', error)
  }
}

const handleSyncFromSource = async (prop: PhysicalProperty) => {
  if (prop.id) {
    try {
      const updated = await physicalPropertyApi.syncFromSource(prop.id)
      ElMessage.success('同步成功')
      loadProperties()
    } catch (error: any) {
      ElMessage.error(error.message || '同步失败')
    }
  }
}

const handleSourcePropertyChange = (sourcePropertyId: string) => {
  const source = sourceProperties.value.find(p => p.id === sourcePropertyId)
  if (source) {
    creatingProperty.value.name = source.name
    creatingProperty.value.code = source.code
    creatingProperty.value.dataType = source.dataType
    creatingProperty.value.description = source.description
    // 自动映射数据库类型
    creatingProperty.value.dbType = mapDataTypeToDbType(source.dataType)
  }
}

const mapDataTypeToDbType = (dataType: string): string => {
  switch (dataType) {
    case 'STRING': return 'VARCHAR'
    case 'INTEGER': return 'INT'
    case 'LONG': return 'BIGINT'
    case 'DOUBLE': return 'DECIMAL'
    case 'BOOLEAN': return 'TINYINT'
    case 'DATE': return 'DATE'
    case 'DATETIME': return 'DATETIME'
    case 'OBJECT':
    case 'ARRAY': return 'JSON'
    default: return 'VARCHAR'
  }
}

const dataTypes = ['STRING', 'INTEGER', 'LONG', 'DOUBLE', 'BOOLEAN', 'DATE', 'DATETIME', 'OBJECT', 'ARRAY']
const dbTypes = ['VARCHAR', 'INT', 'BIGINT', 'DECIMAL', 'FLOAT', 'TINYINT', 'DATE', 'DATETIME', 'JSON', 'TEXT', 'LONGTEXT']

onMounted(() => {
  loadSourceData()
  loadProperties()
})
</script>

<template>
  <div class="physical-properties-page">
    <div class="page-header">
      <button class="back-btn" @click="handleGoBack">
        <ArrowLeft />
        返回详情
      </button>
      <h2 class="page-title">扩展属性管理</h2>
      <button class="create-btn" @click="handleCreate">
        <Plus />
        新建属性
      </button>
    </div>

    <div class="properties-container">
      <div class="properties-list" v-if="properties.length > 0">
        <div class="property-card" v-for="prop in properties" :key="prop.id">
          <div class="property-header">
            <div class="property-info">
              <span class="property-name">{{ prop.name }}</span>
              <span class="property-code">{{ prop.code }}</span>
            </div>
            <div class="property-actions">
              <button class="action-btn sync" @click="handleSyncFromSource(prop)" v-if="prop.sourcePropertyId">
                同步源
              </button>
              <button class="action-btn edit" @click="handleEdit(prop)">
                <Edit />
              </button>
              <button class="action-btn delete" @click="handleDelete(prop.id!)">
                <Delete />
              </button>
            </div>
          </div>
          <div class="property-body">
            <div class="field">
              <label>业务类型</label>
              <span class="value">{{ prop.dataType || '-' }}</span>
            </div>
            <div class="field">
              <label>数据库类型</label>
              <span class="value">{{ prop.dbType || '-' }}</span>
            </div>
            <div class="field" v-if="prop.dbLength">
              <label>长度</label>
              <span class="value">{{ prop.dbLength }}</span>
            </div>
            <div class="field">
              <label>可空</label>
              <span class="value">{{ prop.nullable ? '是' : '否' }}</span>
            </div>
            <div class="field">
              <label>主键</label>
              <span class="value">{{ prop.isPrimaryKey ? '是' : '否' }}</span>
            </div>
            <div class="field" v-if="prop.sourcePropertyName">
              <label>源属性</label>
              <span class="value source">{{ prop.sourcePropertyName }}</span>
            </div>
          </div>
          <div class="property-description" v-if="prop.description">
            {{ prop.description }}
          </div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <p>暂无扩展属性</p>
        <button class="create-btn" @click="handleCreate">
          <Plus />
          添加第一个属性
        </button>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="showCreateDialog" :title="editingProperty ? '编辑属性' : '新建属性'" width="600px">
      <el-form :model="creatingProperty" label-width="120px">
        <el-form-item label="源属性">
          <el-select 
            v-model="creatingProperty.sourcePropertyId" 
            placeholder="选择源属性（可选）" 
            clearable
            style="width: 100%"
            @change="handleSourcePropertyChange"
          >
            <el-option v-for="sp in sourceProperties" :key="sp.id" :label="sp.name" :value="sp.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="属性名称" required>
          <el-input v-model="creatingProperty.name" placeholder="请输入属性名称" />
        </el-form-item>
        <el-form-item label="属性编号">
          <el-input v-model="creatingProperty.code" placeholder="请输入属性编号" />
        </el-form-item>
        <el-form-item label="业务类型" required>
          <el-select v-model="creatingProperty.dataType" style="width: 100%">
            <el-option v-for="dt in dataTypes" :key="dt" :label="dt" :value="dt" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据库类型">
          <el-select v-model="creatingProperty.dbType" placeholder="自动映射或手动选择" style="width: 100%" clearable>
            <el-option v-for="db in dbTypes" :key="db" :label="db" :value="db" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据库长度" v-if="['VARCHAR', 'CHAR'].includes(creatingProperty.dbType)">
          <el-input-number v-model="creatingProperty.dbLength" :min="1" :max="4096" />
        </el-form-item>
        <el-form-item label="精度" v-if="creatingProperty.dbType === 'DECIMAL'">
          <div style="display: flex; gap: 12px;">
            <el-input-number v-model="creatingProperty.dbPrecision" :min="1" :max="65" placeholder="精度" />
            <el-input-number v-model="creatingProperty.dbScale" :min="0" :max="30" placeholder="小数位" />
          </div>
        </el-form-item>
        <el-form-item label="可空">
          <el-switch v-model="creatingProperty.nullable" />
        </el-form-item>
        <el-form-item label="主键">
          <el-switch v-model="creatingProperty.isPrimaryKey" />
        </el-form-item>
        <el-form-item label="索引">
          <el-switch v-model="creatingProperty.isIndex" />
        </el-form-item>
        <el-form-item label="默认值">
          <el-input v-model="creatingProperty.defaultValue" placeholder="默认值（可选）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="creatingProperty.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.physical-properties-page {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
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

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: #1e3a5f;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #2d4a6f;
  }

  svg {
    width: 16px;
    height: 16px;
  }
}

.properties-container {
  min-height: 400px;
}

.properties-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.property-card {
  background-color: #fafbfc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.property-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.property-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.property-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.property-code {
  font-size: 12px;
  color: #999;
  font-family: 'Courier New', monospace;
}

.property-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;

  svg {
    width: 14px;
    height: 14px;
  }

  &.sync {
    background-color: #e3f2fd;
    color: #2196f3;
    font-size: 12px;
    padding: 4px 8px;

    &:hover {
      background-color: #bbdefb;
    }
  }

  &.edit {
    background-color: #fff3e0;

    svg {
      color: #ff9800;
    }

    &:hover {
      background-color: #ffe0b2;
    }
  }

  &.delete {
    background-color: #ffebee;

    svg {
      color: #f44336;
    }

    &:hover {
      background-color: #ffcdd2;
    }
  }
}

.property-body {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 2px;

  label {
    font-size: 12px;
    color: #999;
  }

  .value {
    font-size: 13px;
    color: #333;

    &.source {
      color: #2196f3;
    }
  }
}

.property-description {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #666;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #999;

  p {
    margin-bottom: 16px;
  }

  .create-btn {
    background-color: #f5f7fa;
    color: #666;

    &:hover {
      background-color: #e8ebf0;
    }
  }
}
</style>
