<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { propertyApi } from '@/api/property'
import { requirementApi } from '@/api/requirement'
import { Search, Edit, Delete, Refresh, Plus } from '@element-plus/icons-vue'
import { ElTooltip } from 'element-plus'

const router = useRouter()
const route = useRoute()
const modelId = route.params.id as string

const properties = ref<any[]>([])
const total = ref(0)
const page = ref(0)
const size = ref(10)
const searchName = ref('')
const requirements = ref<any[]>([])

const loadProperties = async () => {
  try {
    const res = await propertyApi.list(modelId, { page: page.value, size: size.value, name: searchName.value })
    properties.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load properties:', error)
  }
}

const loadRequirements = async () => {
  try {
    const res = await requirementApi.list({ page: 0, size: 100 })
    requirements.value = res.content || []
  } catch (error) {
    console.error('Failed to load requirements:', error)
  }
}

const handleSearch = () => {
  page.value = 0
  loadProperties()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadProperties()
}

const handleEdit = (id: string) => {
  router.push(`/models/${modelId}/properties/${id}/edit`)
}

const handleDelete = async (id: string) => {
  if (confirm('确定要删除这个属性吗？')) {
    try {
      await propertyApi.delete(id)
      loadProperties()
    } catch (error) {
      console.error('Failed to delete property:', error)
    }
  }
}

const handleCreate = () => {
  router.push(`/models/${modelId}/properties/create`)
}

const handleBack = () => {
  router.push(`/models/${modelId}`)
}

const getRequirementName = (id: string) => {
  const req = requirements.value.find(r => r.id === id)
  return req?.name || id
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    STRING: '字符串',
    INTEGER: '整数',
    LONG: '长整数',
    DOUBLE: '浮点数',
    BOOLEAN: '布尔值',
    DATE: '日期',
    DATETIME: '日期时间',
    ENUM: '枚举',
    OBJECT: '对象',
    ARRAY: '数组'
  }
  return map[type] || type
}

onMounted(() => {
  loadProperties()
  loadRequirements()
})
</script>

<template>
  <div class="property-list">
    <div class="list-header">
      <button class="back-btn" @click="handleBack">返回模型详情</button>
      <h2>属性管理</h2>
    </div>
    
    <div class="search-bar">
      <div class="search-input-wrapper">
        <Search class="search-icon" />
        <input
          v-model="searchName"
          type="text"
          placeholder="搜索属性名称..."
          @keyup.enter="handleSearch"
        />
      </div>
      <button class="search-btn" @click="handleSearch">搜索</button>
      <button class="refresh-btn" @click="loadProperties">
        <Refresh />
      </button>
      <button class="create-btn" @click="handleCreate">
        <Plus />
        <span>新建属性</span>
      </button>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>属性名称</th>
            <th>属性编码</th>
            <th>数据类型</th>
            <th>关联需求</th>
            <th>是否必填</th>
            <th>默认值</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="prop in properties" :key="prop.id">
            <td>{{ prop.name }}</td>
            <td>{{ prop.code }}</td>
            <td>{{ getTypeName(prop.dataType) }}</td>
            <td>{{ prop.requirement ? getRequirementName(prop.requirement.id) : '-' }}</td>
            <td>{{ prop.required ? '是' : '否' }}</td>
            <td>{{ prop.defaultValue || '-' }}</td>
            <td>{{ prop.createdAt?.slice(0, 10) }}</td>
            <td class="actions">
              <el-tooltip content="编辑" placement="top">
                <button class="action-btn edit" @click="handleEdit(prop.id)">
                  <Edit />
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="action-btn delete" @click="handleDelete(prop.id)">
                  <Delete />
                </button>
              </el-tooltip>
            </td>
          </tr>
          <tr v-if="properties.length === 0">
            <td colspan="8" class="empty-row">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div class="pagination">
      <span class="total">共 {{ total }} 条</span>
      <div class="page-buttons">
        <button 
          :disabled="page === 0" 
          @click="page > 0 && handlePageChange(page - 1)"
          class="page-btn"
        >
          上一页
        </button>
        <span class="page-info">{{ page + 1 }} / {{ Math.ceil(total / size) || 1 }}</span>
        <button 
          :disabled="(page + 1) * size >= total" 
          @click="(page + 1) * size < total && handlePageChange(page + 1)"
          class="page-btn"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.property-list {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.list-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  padding: 10px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: #1e3a5f;
    color: #1e3a5f;
  }
}

.list-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 10px;
  padding: 12px 16px;
}

.search-icon {
  font-size: 16px;
  color: #999;
  margin-right: 12px;
  width: 16px;
  height: 16px;
  
  & svg {
    width: 16px !important;
    height: 16px !important;
  }
}

.search-input-wrapper input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
  
  &::placeholder {
    color: #999;
  }
}

.search-btn {
  padding: 12px 24px;
  background-color: #1e3a5f;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  
  &:hover {
    background-color: #2d4a6f;
  }
}

.refresh-btn {
  padding: 12px;
  background-color: #f5f7fa;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  
  &:hover {
    background-color: #e8ebf0;
  }
  
  svg {
    font-size: 18px;
    color: #666;
    width: 18px;
    height: 18px;
  }
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background-color: #1e3a5f;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  
  &:hover {
    background-color: #2d4a6f;
  }
  
  svg {
    font-size: 16px;
  }
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.data-table th {
  background-color: #fafbfc;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: background-color 0.2s ease;
  
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
  
  svg {
    font-size: 16px;
    width: 16px;
    height: 16px;
  }
}

.empty-row {
  text-align: center;
  color: #999;
  padding: 40px;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.total {
  font-size: 14px;
  color: #666;
}

.page-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover:not(:disabled) {
    border-color: #1e3a5f;
    color: #1e3a5f;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.page-info {
  font-size: 14px;
  color: #666;
}
</style>
