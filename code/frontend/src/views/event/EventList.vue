<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { eventApi } from '@/api/event'
import { Search, Edit, Delete, Refresh, View } from '@element-plus/icons-vue'
import { ElTooltip } from 'element-plus'

const router = useRouter()
const events = ref<any[]>([])
const total = ref(0)
const page = ref(0)
const size = ref(10)
const searchName = ref('')

const loadEvents = async () => {
  try {
    const params: any = { page: page.value, size: size.value }
    if (searchName.value) {
      params.name = searchName.value
    }
    const res = await eventApi.list(params)
    events.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load events:', error)
  }
}

const handleSearch = () => {
  page.value = 0
  loadEvents()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadEvents()
}

const handleView = (id: string) => {
  router.push(`/events/${id}`)
}

const handleEdit = (id: string) => {
  router.push(`/events/${id}/edit`)
}

const handleDelete = async (id: string) => {
  if (confirm('确定要删除这个事件吗？')) {
    try {
      await eventApi.delete(id)
      loadEvents()
    } catch (error) {
      console.error('Failed to delete event:', error)
    }
  }
}

const handleRefresh = () => {
  searchName.value = ''
  page.value = 0
  loadEvents()
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    ORDER_CREATED: '订单创建',
    ORDER_PAID: '订单支付',
    ORDER_SHIPPED: '订单发货',
    ORDER_COMPLETED: '订单完成',
    PAYMENT_SUCCESS: '支付成功',
    PAYMENT_FAILED: '支付失败',
    REFUND_REQUESTED: '退款申请',
    REFUND_COMPLETED: '退款完成',
    USER_REGISTERED: '用户注册',
    USER_LOGIN: '用户登录',
    USER_LOGOUT: '用户登出',
    PRODUCT_VIEWED: '商品浏览',
    PRODUCT_ADDED: '商品加入购物车',
    INVENTORY_LOW: '库存预警',
    SYSTEM_ERROR: '系统错误',
    SYSTEM_WARNING: '系统警告'
  }
  return map[type] || type
}

onMounted(() => {
  loadEvents()
})
</script>

<template>
  <div class="event-list">
    <div class="search-bar">
      <div class="search-input-wrapper">
        <Search class="search-icon" />
        <input
          v-model="searchName"
          type="text"
          placeholder="搜索事件名称..."
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
            <th>事件名称</th>
            <th>事件类型</th>
            <th>关联模型</th>
            <th>金额</th>
            <th>数量</th>
            <th>事件时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="event in events" :key="event.id">
            <td>{{ event.name }}</td>
            <td>{{ getTypeName(event.eventType) }}</td>
            <td>{{ event.model?.name || '-' }}</td>
            <td>¥{{ (event.amount || 0).toLocaleString() }}</td>
            <td>{{ event.quantity || 0 }}</td>
            <td>{{ event.eventTime?.slice(0, 19) }}</td>
            <td>
              <span :class="'status-tag ' + event.status.toLowerCase()">
                {{ event.status === 'SUCCESS' ? '成功' : event.status === 'FAILED' ? '失败' : event.status === 'PENDING' ? '处理中' : event.status }}
              </span>
            </td>
            <td class="actions">
              <el-tooltip content="查看" placement="top">
                <button class="action-btn view" @click="handleView(event.id)">
                  <View />
                </button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <button class="action-btn edit" @click="handleEdit(event.id)">
                  <Edit />
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="action-btn delete" @click="handleDelete(event.id)">
                  <Delete />
                </button>
              </el-tooltip>
            </td>
          </tr>
          <tr v-if="events.length === 0">
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
.event-list {
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

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  
  &.success {
    background-color: #e8f5e9;
    color: #4caf50;
  }
  
  &.failed {
    background-color: #ffebee;
    color: #f44336;
  }
  
  &.pending {
    background-color: #e3f2fd;
    color: #2196f3;
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
