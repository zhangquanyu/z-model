<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { requirementApi } from '@/api/requirement'
import { modelApi } from '@/api/model'
import { Document, Box, ArrowUpBold } from '@element-plus/icons-vue'

const router = useRouter()
const stats = ref({
  requirements: 0,
  models: 0
})

const recentRequirements = ref<any[]>([])
const recentModels = ref<any[]>([])

onMounted(async () => {
  try {
    const reqRes = await requirementApi.list({ page: 0, size: 5 })
    stats.value.requirements = reqRes.totalElements || 0
    recentRequirements.value = reqRes.content || []

    const modelRes = await modelApi.list({ page: 0, size: 5 })
    stats.value.models = modelRes.totalElements || 0
    recentModels.value = modelRes.content || []
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
})

const handleNavigate = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card" @click="handleNavigate('/requirements')">
        <div class="stat-icon requirement">
          <Document />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.requirements }}</div>
          <div class="stat-label">需求数量</div>
        </div>
      </div>
      
      <div class="stat-card" @click="handleNavigate('/models')">
        <div class="stat-icon model">
          <Box />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.models }}</div>
          <div class="stat-label">模型数量</div>
        </div>
      </div>
    </div>
    
    <div class="dashboard-grid">
      <div class="dashboard-card">
        <div class="card-header">
          <h3>最近需求</h3>
          <button class="view-all" @click="handleNavigate('/requirements')">查看全部</button>
        </div>
        <div class="card-content">
          <div v-if="recentRequirements.length === 0" class="empty-state">
            暂无需求数据
          </div>
          <div v-else class="list-items">
            <div v-for="item in recentRequirements" :key="item.id" class="list-item">
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-meta">{{ item.status }} · {{ item.createdAt?.slice(0, 10) }}</div>
              </div>
              <span :class="'status-tag ' + item.status.toLowerCase()">{{ item.status }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="dashboard-card">
        <div class="card-header">
          <h3>最近模型</h3>
          <button class="view-all" @click="handleNavigate('/models')">查看全部</button>
        </div>
        <div class="card-content">
          <div v-if="recentModels.length === 0" class="empty-state">
            暂无模型数据
          </div>
          <div v-else class="list-items">
            <div v-for="item in recentModels" :key="item.id" class="list-item">
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-meta">{{ item.code }} · {{ item.createdAt?.slice(0, 10) }}</div>
              </div>
              <span class="model-tag">{{ item.requirements?.length || 0 }}个需求</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-card {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  
  &.requirement {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  &.model {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
  
  &.event {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  &.amount {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1e3a5f;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.dashboard-card {
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}

.view-all {
  padding: 6px 16px;
  border: none;
  background-color: #f5f7fa;
  color: #1e3a5f;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  
  &:hover {
    background-color: #e8ebf0;
  }
}

.card-content {
  padding: 20px 24px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.list-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #fafbfc;
  border-radius: 10px;
  transition: background-color 0.2s ease;
  
  &:hover {
    background-color: #f0f2f5;
  }
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.item-meta {
  font-size: 12px;
  color: #999;
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

.model-tag {
  padding: 4px 12px;
  background-color: #f5f7fa;
  color: #666;
  border-radius: 20px;
  font-size: 12px;
}
</style>
