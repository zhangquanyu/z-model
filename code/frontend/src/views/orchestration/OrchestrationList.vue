<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, View } from '@element-plus/icons-vue'
import { orchestrationApi, type OrchestrationSummary } from '@/api/orchestration'

const router = useRouter()
const list = ref<OrchestrationSummary[]>([])
const total = ref(0)
const keyword = ref('')
const page = ref(0)
const size = ref(10)
const loading = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await orchestrationApi.list(keyword.value, page.value, size.value)
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
  showCreateDialog.value = true
  createForm.value.name = ''
  createForm.value.code = ''
  createForm.value.description = ''
}

const handleView = (id: string) => {
  router.push(`/orchestrations/${id}`)
}

const handleDesign = (id: string) => {
  router.push(`/orchestrations/${id}/design`)
}

const handleEdit = (id: string) => {
  router.push(`/orchestrations/${id}/edit`)
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除该业务编排吗？删除后将同时删除所有编排节点和关联数据。', '提示', {
      type: 'warning'
    })
    await orchestrationApi.delete(id)
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
  return map[status] || { type: 'info', label: status || '草稿' }
}

const showCreateDialog = ref(false)
const createForm = ref({ name: '', code: '', description: '' })

const handleConfirmCreate = async () => {
  if (!createForm.value.name) {
    ElMessage.warning('请输入编排名称')
    return
  }
  try {
    const res = await orchestrationApi.create({
      name: createForm.value.name,
      code: createForm.value.code || undefined,
      description: createForm.value.description || undefined
    })
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    if (res?.id) {
      router.push(`/orchestrations/${res.id}/design`)
    } else {
      loadList()
    }
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

onMounted(() => {
  loadList()
})
</script>

<template>
  <div class="orchestration-list">
    <div class="toolbar">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索编排名称或编码"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          style="width: 300px"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建编排</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="code" label="编排编码" width="140" />
      <el-table-column prop="name" label="编排名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="节点数" width="100" align="center">
        <template #default="{ row }">
          <span>{{ row.nodeCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="方法数" width="100" align="center">
        <template #default="{ row }">
          <span>{{ row.methodCount || 0 }}</span>
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
      <el-table-column label="操作" width="280" fixed="right">
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

    <el-dialog v-model="showCreateDialog" title="新建业务编排" width="500px" @close="showCreateDialog = false">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="编排名称" required>
          <el-input v-model="createForm.name" placeholder="请输入编排名称" />
        </el-form-item>
        <el-form-item label="编排编码">
          <el-input v-model="createForm.code" placeholder="留空自动生成，如 ORCH-0001" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入编排描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmCreate">创建并设计</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.orchestration-list {
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
