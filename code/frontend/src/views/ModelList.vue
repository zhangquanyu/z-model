<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { modelApi } from '@/api/model'
import { Search, Edit, Trash2, Eye, Refresh, Settings } from '@element-plus/icons-vue'

const router = useRouter()
const models = ref<any[]>([])
const total = ref(0)
const page = ref(0)
const size = ref(10)
const searchName = ref('')

const loadModels = async () => {
  try {
    const params: any = { page: page.value, size: size.value }
    if (searchName.value) {
      params.name = searchName.value
    }
    const res = await modelApi.list(params)
    models.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load models:', error)
  }
}

const handleSearch = () => {
  page.value = 0
  loadModels()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadModels()
}

const handleView = (id: string) => {
  router.push(`/models/${id}`)
}

const handleEdit = (id: string) => {
  router.push(`/models/${id}/edit`)
}

const handleDelete = async (id: string) => {
  if (confirm('确定要删除这个模型吗？')) {
    try {
      await modelApi.delete(id)
      loadModels()
    } catch (error) {
      console.error('Failed to delete model:', error)
    }
  }
}

const handleManageProperties = (id: string) => {
  router.push(`/models/${id}/properties`)
}

const handleManageMethods = (id: string) => {
  router.push(`/models/${id}/methods`)
}

const handleRefresh = () => {
  searchName.value = ''
  page.value = 0
  loadModels()
}

onMounted(() => {
  loadModels()
})
</script>

<template>
  <div class="model-list">
    <div class="search-bar">
      <div class="search-input-wrapper">
        <Search class="search-icon" />
        <input
          v-model="searchName"
          type="text"
          placeholder="搜索模型名称..."
          @keyup.enter="handleSearch"
        />
      </div>
      <button class="search-btn" @click="handleSearch">搜索</button>
      <button class="refresh-btn" @click="handleRefresh">
        <Refresh />
      </button>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>模型名称</th>
            <th>模型编号</th>
            <th>描述</th>
            <th>关联需求</th>
            <th>属性数量</th>
            <th>方法数量</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="model in models" :key="model.id">
            <td>{{ model.name }}</td>
            <td>{{ model.code }}</td>
            <td class="description-cell">{{ model.description || '-' }}</td>
            <td>
              <span class="tag">{{ model.requirements?.length || 0 }}个</span>
            </td>
            <td>{{ model.properties?.length || 0 }}</td>
            <td>{{ model.methods?.length || 0 }}</td>
            <td>{{ model.createdAt?.slice(0, 10) }}</td>
            <td class="actions">
              <button class="action-btn view" @click="handleView(model.id)">
                <Eye />
              </button>
              <button class="action-btn edit" @click="handleEdit(model.id)">
                <Edit />
              </button>
              <button class="action-btn property" @click="handleManageProperties(model.id)">
                <Settings />
              </button>
              <button class="action-btn delete" @click="handleDelete(model.id)">
                <Trash2 />
              </button>
            </td>
          </tr>
          <tr v-if="models.length === 0">
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
.model-list {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
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
  font-size: 18px;
  color: #999;
  margin-right: 12px;
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

.description-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag {
  padding: 4px 12px;
  background-color: #e8f5e9;
  color: #4caf50;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  
  &.view {
    background-color: #e3f2fd;
    
    svg {
      color: #2196f3;
    }
    
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
  
  &.property {
    background-color: #e8f5e9;
    
    svg {
      color: #4caf50;
    }
    
    &:hover {
      background-color: #c8e6c9;
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
