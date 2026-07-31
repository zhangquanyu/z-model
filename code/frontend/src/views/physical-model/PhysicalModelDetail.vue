<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { physicalModelApi, physicalPropertyApi, physicalMethodApi, type PhysicalModel, type PhysicalProperty, type PhysicalMethod, type GenerateSQLResponse } from '@/api/physical-model'
import { modelApi } from '@/api/model'
import { ArrowLeft, Edit, Delete, Plus, Refresh, Setting, Files } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const physicalModelId = route.params.id as string

const physicalModel = ref<PhysicalModel | null>(null)
const businessModelName = ref('')
const sqlResult = ref<GenerateSQLResponse | null>(null)
const showSQLDialog = ref(false)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const data = await physicalModelApi.getById(physicalModelId)
    physicalModel.value = data
    // 获取业务模型名称
    if (data.modelId) {
      const bm = await modelApi.getById(data.modelId)
      businessModelName.value = bm.name || ''
    }
  } catch (error) {
    console.error('Failed to load physical model:', error)
  } finally {
    loading.value = false
  }
}

const handleGenerateSQL = async () => {
  try {
    sqlResult.value = await physicalModelApi.generateSQL(physicalModelId)
    showSQLDialog.value = true
  } catch (error) {
    console.error('Failed to generate SQL:', error)
  }
}

const handleEdit = () => {
  router.push(`/physical-models/${physicalModelId}/edit`)
}

const handleDelete = async () => {
  if (confirm('确定要删除这个物理模型吗？')) {
    try {
      await physicalModelApi.delete(physicalModelId)
      router.push('/physical-models')
    } catch (error) {
      console.error('Failed to delete physical model:', error)
    }
  }
}

const handleManageProperties = () => {
  router.push(`/physical-models/${physicalModelId}/properties`)
}

const handleManageMethods = () => {
  router.push(`/physical-models/${physicalModelId}/methods`)
}

const handleGoBack = () => {
  router.push('/physical-models')
}

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text)
    .then(() => alert('已复制到剪贴板'))
    .catch(() => alert('复制失败'))
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="physical-model-detail" v-if="physicalModel">
    <div class="page-header">
      <button class="back-btn" @click="handleGoBack">
        <ArrowLeft />
        返回
      </button>
      <h2 class="page-title">{{ physicalModel.name }}</h2>
      <div class="header-actions">
        <button class="action-btn generate-sql" @click="handleGenerateSQL">
          <Code />
          生成 SQL
        </button>
        <button class="action-btn edit" @click="handleEdit">
          <Edit />
          编辑
        </button>
        <button class="action-btn delete" @click="handleDelete">
          <Delete />
          删除
        </button>
      </div>
    </div>

    <div class="content-sections">
      <!-- 基本信息 -->
      <div class="section">
        <h3 class="section-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">物理模型名称</span>
            <span class="value">{{ physicalModel.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">编号</span>
            <span class="value">{{ physicalModel.code || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">关联业务模型</span>
            <span class="value link" @click="businessModelName && router.push(`/models/${physicalModel.modelId}`)">{{ businessModelName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">数据库表名</span>
            <span class="value">{{ physicalModel.tableName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态</span>
            <span :class="['status-tag', physicalModel.status?.toLowerCase() === 'completed' ? 'completed' : physicalModel.status?.toLowerCase() === 'designing' ? 'designing' : 'draft']">
              {{ physicalModel.status === 'COMPLETED' ? '已完成' : physicalModel.status === 'DESIGNING' ? '设计中' : '草稿' }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">创建时间</span>
            <span class="value">{{ physicalModel.createdAt?.replace('T', ' ').slice(0, 19) }}</span>
          </div>
          <div class="info-item full-width">
            <span class="label">描述</span>
            <span class="value">{{ physicalModel.description || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 扩展属性 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">扩展属性 ({{ physicalModel.properties?.length || 0 }})</h3>
          <button class="section-action" @click="handleManageProperties">
            <Setting />
            管理属性
          </button>
        </div>
        <div class="properties-list" v-if="physicalModel.properties && physicalModel.properties.length > 0">
          <div class="property-card" v-for="prop in physicalModel.properties" :key="prop.id">
            <div class="property-header">
              <span class="property-name">{{ prop.name }}</span>
              <span class="property-type">{{ prop.dataType }}</span>
            </div>
            <div class="property-body">
              <div class="property-detail">
                <span class="label">数据库类型</span>
                <span class="value">{{ prop.dbType || '-' }}</span>
              </div>
              <div class="property-detail" v-if="prop.dbLength">
                <span class="label">长度</span>
                <span class="value">{{ prop.dbLength }}</span>
              </div>
              <div class="property-detail">
                <span class="label">可空</span>
                <span class="value">{{ prop.nullable ? '是' : '否' }}</span>
              </div>
              <div class="property-detail">
                <span class="label">主键</span>
                <span class="value">{{ prop.isPrimaryKey ? '是' : '否' }}</span>
              </div>
              <div class="property-detail" v-if="prop.sourcePropertyName">
                <span class="label">源属性</span>
                <span class="value">{{ prop.sourcePropertyName }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p>暂无扩展属性，点击"管理属性"添加</p>
        </div>
      </div>

      <!-- 扩展方法 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">扩展方法 ({{ physicalModel.methods?.length || 0 }})</h3>
          <button class="section-action" @click="handleManageMethods">
            <Files />
            管理方法
          </button>
        </div>
        <div class="methods-list" v-if="physicalModel.methods && physicalModel.methods.length > 0">
          <div class="method-card" v-for="method in physicalModel.methods" :key="method.id">
            <div class="method-header">
              <span class="method-name">{{ method.name }}</span>
              <span :class="['method-type', method.methodType?.toLowerCase()]">
                {{ method.methodType }}
              </span>
            </div>
            <div class="method-body">
              <div class="method-detail">
                <span class="label">源方法</span>
                <span class="value">{{ method.sourceMethodName || '-' }}</span>
              </div>
              <div class="method-detail" v-if="method.sqlTemplate">
                <span class="label">SQL 模板</span>
                <pre class="sql-template">{{ method.sqlTemplate }}</pre>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p>暂无扩展方法，点击"管理方法"添加</p>
        </div>
      </div>
    </div>

    <!-- SQL 生成对话框 -->
    <el-dialog v-model="showSQLDialog" title="生成的 SQL 语句" width="800px" max-height="80vh">
      <div class="sql-section" v-if="sqlResult">
        <div class="sql-block">
          <h4>建表语句</h4>
          <pre class="sql-content">{{ sqlResult.createTableSQL }}</pre>
          <button class="copy-btn" @click="copyToClipboard(sqlResult.createTableSQL || '')">复制</button>
        </div>
        
        <div class="sql-block" v-if="sqlResult.methodSQLs && sqlResult.methodSQLs.length > 0">
          <h4>方法 SQL</h4>
          <div v-for="method in sqlResult.methodSQLs" :key="method.id" class="method-sql-item">
            <div class="method-sql-header">
              <span>{{ method.methodType }} - {{ method.name }}</span>
              <button class="copy-btn small" @click="copyToClipboard(method.sql || '')">复制</button>
            </div>
            <pre class="sql-content">{{ method.sql }}</pre>
          </div>
        </div>

        <div class="sql-block">
          <div class="sql-block-header">
            <h4>完整 SQL</h4>
            <button class="copy-btn" @click="copyToClipboard(sqlResult.completeSQL || '')">复制完整 SQL</button>
          </div>
          <pre class="sql-content">{{ sqlResult.completeSQL }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="showSQLDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>

  <div class="loading" v-else-if="loading">
    <p>加载中...</p>
  </div>
</template>

<style lang="scss" scoped>
.physical-model-detail {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
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

.header-actions {
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

  &.generate-sql {
    background-color: #2196f3;
    color: white;

    &:hover {
      background-color: #1976d2;
    }
  }

  &.edit {
    background-color: #fff3e0;
    color: #ff9800;

    &:hover {
      background-color: #ffe0b2;
    }
  }

  &.delete {
    background-color: #ffebee;
    color: #f44336;

    &:hover {
      background-color: #ffcdd2;
    }
  }
}

.content-sections {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.section {
  background-color: #fafbfc;
  border-radius: 12px;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.section-header .section-title {
  margin-bottom: 0;
}

.section-action {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #1e3a5f;
    color: #1e3a5f;
  }

  svg {
    width: 16px;
    height: 16px;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &.full-width {
    grid-column: 1 / -1;
  }

  .label {
    font-size: 12px;
    color: #999;
  }

  .value {
    font-size: 14px;
    color: #333;

    &.link {
      color: #2196f3;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;

  &.draft {
    background-color: #f5f5f5;
    color: #999;
  }

  &.designing {
    background-color: #fff3e0;
    color: #ff9800;
  }

  &.completed {
    background-color: #e8f5e9;
    color: #4caf50;
  }
}

.properties-list,
.methods-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.property-card,
.method-card {
  background-color: white;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.property-header,
.method-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.property-name,
.method-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.property-type {
  padding: 2px 8px;
  background-color: #e3f2fd;
  color: #2196f3;
  border-radius: 4px;
  font-size: 12px;
}

.method-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.select {
    background-color: #e3f2fd;
    color: #2196f3;
  }

  &.insert {
    background-color: #e8f5e9;
    color: #4caf50;
  }

  &.update {
    background-color: #fff3e0;
    color: #ff9800;
  }

  &.delete {
    background-color: #ffebee;
    color: #f44336;
  }
}

.property-body,
.method-body {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
}

.property-detail,
.method-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .label {
    font-size: 12px;
    color: #999;
  }

  .value {
    font-size: 13px;
    color: #333;
  }
}

.sql-template {
  background-color: #f5f5f5;
  border-radius: 4px;
  padding: 8px;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  margin-top: 4px;
}

.empty-state {
  text-align: center;
  padding: 32px;
  color: #999;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  color: #999;
}

.sql-section {
  max-height: 60vh;
  overflow-y: auto;
}

.sql-block {
  margin-bottom: 24px;

  h4 {
    margin-bottom: 12px;
    font-size: 14px;
    color: #333;
  }
}

.sql-block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  h4 {
    margin-bottom: 0;
  }
}

.method-sql-item {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.method-sql-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.sql-content {
  background-color: #f5f5f5;
  border-radius: 8px;
  padding: 16px;
  font-size: 13px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  overflow-x: auto;
  margin: 0;
}

.copy-btn {
  padding: 6px 12px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: #1976d2;
  }

  &.small {
    padding: 4px 8px;
  }
}
</style>
