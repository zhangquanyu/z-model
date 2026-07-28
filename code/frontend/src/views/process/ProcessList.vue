<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, View } from '@element-plus/icons-vue'
import { processApi, type BpmnProcess } from '@/api/process'

const router = useRouter()
const list = ref<BpmnProcess[]>([])
const total = ref(0)
const keyword = ref('')
const page = ref(0)
const size = ref(10)
const loading = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await processApi.list({
      keyword: keyword.value,
      page: page.value,
      size: size.value
    })
    list.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 0
  loadList()
}

const handleCreate = () => {
  router.push('/processes/create')
}

const handleEdit = (id: string) => {
  router.push(`/processes/${id}/edit`)
}

const handleView = (id: string) => {
  router.push(`/processes/${id}`)
}

const handleDesign = (id: string) => {
  router.push(`/processes/${id}/design`)
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除该流程吗？', '提示', {
      type: 'warning'
    })
    await processApi.delete(id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handlePageChange = (val: number) => {
  page.value = val - 1
  loadList()
}

const getStatusTag = (status: string) => {
  const map: Record<string, { type: string; label: string }> = {
    'DRAFT': { type: 'info', label: '草稿' },
    'ACTIVE': { type: 'success', label: '已启用' },
    'ARCHIVED': { type: 'warning', label: '已归档' }
  }
  return map[status] || { type: 'info', label: status }
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="process-list">
    <div class="toolbar">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索流程名称或编码"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          style="width: 300px"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建流程</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="code" label="流程编码" width="120" />
      <el-table-column prop="name" label="流程名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="版本" width="80" align="center">
        <template #default="{ row }">
          <span>v{{ row.version }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status).type">
            {{ getStatusTag(row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180">
        <template #default="{ row }">
          {{ row.updatedAt ? new Date(row.updatedAt).toLocaleString('zh-CN') : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :icon="View" @click="handleView(row.id!)">查看</el-button>
          <el-button type="success" link :icon="Edit" @click="handleDesign(row.id!)">设计</el-button>
          <el-button type="warning" link @click="handleEdit(row.id!)">编辑</el-button>
          <el-button type="danger" link :icon="Delete" @click="handleDelete(row.id!)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        :current-page="page + 1"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.process-list {
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .search-bar {
    display: flex;
    gap: 10px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
