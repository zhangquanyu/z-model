<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { requirementApi } from '@/api/requirement'
import { Search, Edit, Delete, View, Refresh } from '@element-plus/icons-vue'
import { ElTooltip } from 'element-plus'

const router = useRouter()
const requirements = ref<any[]>([])
const searchName = ref('')

const statusMap: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '待审批',
  APPROVED: '已批准',
  REJECTED: '已拒绝'
}

const priorityMap: Record<string, string> = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  CRITICAL: '紧急'
}

const loadRequirements = async () => {
  try {
    const res = await requirementApi.listMainRequirements(searchName.value || undefined)
    requirements.value = res || []
  } catch (error) {
    console.error('Failed to load requirements:', error)
  }
}

const handleSearch = () => {
  loadRequirements()
}

const handleView = (id: string) => {
  router.push(`/requirements/${id}`)
}

const handleEdit = (id: string) => {
  router.push(`/requirements/${id}/edit`)
}

const handleDelete = async (id: string) => {
  if (confirm('确定要删除这个需求吗？')) {
    try {
      await requirementApi.delete(id)
      loadRequirements()
    } catch (error) {
      console.error('Failed to delete requirement:', error)
    }
  }
}

const handleRefresh = () => {
  searchName.value = ''
  loadRequirements()
}

onMounted(() => {
  loadRequirements()
})
</script>

<template>
  <div class="requirement-list">
    <div class="search-bar">
      <div class="search-input-wrapper">
        <Search class="search-icon" />
        <input
          v-model="searchName"
          type="text"
          placeholder="搜索需求名称..."
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
            <th>需求名称</th>
            <th>需求编号</th>
            <th>状态</th>
            <th>优先级</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in requirements" :key="req.id">
            <td class="name-cell">{{ req.name }}</td>
            <td>{{ req.code }}</td>
            <td>
              <span :class="'status-tag ' + req.status.toLowerCase()">
                {{ statusMap[req.status] || req.status }}
              </span>
            </td>
            <td>{{ priorityMap[req.priority] || req.priority }}</td>
            <td>{{ req.createdAt?.slice(0, 10) }}</td>
            <td class="actions">
              <el-tooltip content="详情" placement="top">
                <button class="action-btn view" @click="handleView(req.id)">
                  <View />
                </button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <button class="action-btn edit" @click="handleEdit(req.id)">
                  <Edit />
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="action-btn delete" @click="handleDelete(req.id)">
                  <Delete />
                </button>
              </el-tooltip>
            </td>
          </tr>
          <tr v-if="requirements.length === 0">
            <td colspan="6" class="empty-row">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.requirement-list {
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
  width: 18px;
  height: 18px;
  
  & svg {
    width: 18px !important;
    height: 18px !important;
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

.name-cell {
  font-weight: 500;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  
  &.draft {
    background-color: #fff3e0;
    color: #ff9800;
  }
  
  &.pending {
    background-color: #e3f2fd;
    color: #2196f3;
  }
  
  &.approved {
    background-color: #e8f5e9;
    color: #4caf50;
  }
  
  &.rejected {
    background-color: #ffebee;
    color: #f44336;
  }
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
</style>