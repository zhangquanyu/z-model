<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { functionalOrchestrationApi, type FunctionalOrchestration } from '@/api/functional-orchestration';
import { orchestrationApi } from '@/api/orchestration';
import { Search, Edit, Delete, View, Plus, Refresh, Setting, DocumentCopy } from '@element-plus/icons-vue';
const router = useRouter();
const orchestrations = ref<FunctionalOrchestration[]>([]);
const businessOrchestrations = ref<any[]>([]);
const total = ref(0);
const page = ref(0);
const size = ref(10);
const searchName = ref('');
const showCreateDialog = ref(false);
const creatingOrchestration = ref<FunctionalOrchestration>({
 orchestrationId: '',
 name: '',
 code: '',
 description: ''
});

// 获取已被关联的业务编排ID列表
const linkedOrchestrationIds = computed(() => {
 return orchestrations.value
 .filter(o => o.orchestrationId)
 .map(o => o.orchestrationId);
});

const loadBusinessOrchestrations = async () => {
 try {
 const res = await orchestrationApi.list({});
 businessOrchestrations.value = res.content || [];
 }
 catch (error) {
 console.error('Failed to load business orchestrations:', error);
 }
};
const loadOrchestrations = async () => {
 try {
 const params: any = { page: page.value, size: size.value };
 if (searchName.value) {
 params.keyword = searchName.value;
 }
 const res = await functionalOrchestrationApi.list(params);
 orchestrations.value = res.content || [];
 total.value = res.totalElements || 0;
 }
 catch (error) {
 console.error('Failed to load functional orchestrations:', error);
 }
};
const handleSearch = () => {
 page.value = 0;
 loadOrchestrations();
};
const handlePageChange = (newPage: number) => {
 page.value = newPage;
 loadOrchestrations();
};
const handleView = (id: string) => {
 router.push(`/functional-orchestrations/${id}`);
};
const handleEdit = (id: string) => {
 router.push(`/functional-orchestrations/${id}/edit`);
};
const handleDesign = (id: string) => {
 router.push(`/functional-orchestrations/${id}/design`);
};
const handleDelete = async (id: string) => {
 if (confirm('确定要删除这个功能编排吗？')) {
 try {
 await functionalOrchestrationApi.delete(id);
 loadOrchestrations();
 }
 catch (error) {
 console.error('Failed to delete functional orchestration:', error);
 }
 }
};
const handleGenerateCode = async (id: string) => {
 router.push(`/functional-orchestrations/${id}/generate-code`);
};
const handleRefresh = () => {
 searchName.value = '';
 page.value = 0;
 loadOrchestrations();
};
const handleCreate = () => {
 creatingOrchestration.value = {
 orchestrationId: '',
 name: '',
 code: '',
 description: ''
 };
 showCreateDialog.value = true;
};
const handleCreateSubmit = async () => {
 try {
 // 清理空字符串，将空字符串转为 null
 const data = { ...creatingOrchestration.value };
 if (!data.orchestrationId || data.orchestrationId.trim() === '') {
 delete data.orchestrationId;
 }
 await functionalOrchestrationApi.create(data);
 showCreateDialog.value = false;
 loadOrchestrations();
 }
 catch (error) {
 console.error('Failed to create functional orchestration:', error);
 }
};
const getOrchestrationName = (orchestrationId: string) => {
 const o = businessOrchestrations.value.find(b => b.id === orchestrationId);
 return o?.name || '-';
};
onMounted(() => {
 loadBusinessOrchestrations();
 loadOrchestrations();
});
</script>

<template>
  <div class="functional-orchestration-list">
    <div class="header">
      <h2 class="page-title">功能编排</h2>
      <button class="create-btn" @click="handleCreate">
        <Plus />
        新建功能编排
      </button>
    </div>

    <div class="search-bar">
      <div class="search-input-wrapper">
        <Search class="search-icon" />
        <input
          v-model="searchName"
          type="text"
          placeholder="搜索功能编排名称..."
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
            <th>功能编排名称</th>
            <th>编号</th>
            <th>关联业务编排</th>
            <th>状态</th>
            <th>节点数量</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="fo in orchestrations" :key="fo.id">
            <td>{{ fo.name }}</td>
            <td>{{ fo.code || '-' }}</td>
            <td>{{ getOrchestrationName(fo.orchestrationId) }}</td>
            <td>
              <span :class="['status-tag', fo.status?.toLowerCase() === 'completed' ? 'completed' : fo.status?.toLowerCase() === 'designing' ? 'designing' : 'draft']">
                {{ fo.status === 'COMPLETED' ? '已完成' : fo.status === 'DESIGNING' ? '设计中' : '草稿' }}
              </span>
            </td>
            <td>{{ fo.nodes?.length || 0 }}</td>
            <td>{{ fo.createdAt?.slice(0, 10) }}</td>
            <td class="actions">
              <el-tooltip content="查看" placement="top">
                <button class="action-btn view" @click="handleView(fo.id!)">
                  <View />
                </button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <button class="action-btn edit" @click="handleEdit(fo.id!)">
                  <Edit />
                </button>
              </el-tooltip>
              <el-tooltip content="编排设计" placement="top">
                <button class="action-btn design" @click="handleDesign(fo.id!)">
                  <Setting />
                </button>
              </el-tooltip>
              <el-tooltip content="生成代码" placement="top">
                <button class="action-btn code" @click="handleGenerateCode(fo.id!)">
                  <DocumentCopy />
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="action-btn delete" @click="handleDelete(fo.id!)">
                  <Delete />
                </button>
              </el-tooltip>
            </td>
          </tr>
          <tr v-if="orchestrations.length === 0">
            <td colspan="7" class="empty-row">暂无数据</td>
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

    <!-- 创建对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建功能编排" width="500px">
      <el-form :model="creatingOrchestration" label-width="120px">
        <el-form-item label="关联业务编排">
          <el-select v-model="creatingOrchestration.orchestrationId" placeholder="可选，选择关联的业务编排" clearable filterable style="width: 100%">
            <el-option 
              v-for="bo in businessOrchestrations" 
              :key="bo.id" 
              :label="linkedOrchestrationIds.includes(bo.id) ? `${bo.name} (已关联)` : bo.name" 
              :value="bo.id"
              :disabled="linkedOrchestrationIds.includes(bo.id)"
            />
          </el-select>
          <div class="form-tip">选填：如不选择则创建独立功能编排；已被关联的业务编排无法重复关联</div>
        </el-form-item>
        <el-form-item label="功能编排名称" required>
          <el-input v-model="creatingOrchestration.name" placeholder="请输入功能编排名称" />
        </el-form-item>
        <el-form-item label="编号">
          <el-input v-model="creatingOrchestration.code" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="creatingOrchestration.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.functional-orchestration-list {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
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
}

.search-input-wrapper input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
}

.search-btn {
  padding: 12px 24px;
  background-color: #1e3a5f;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;

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
  transition: background-color 0.2s;

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

.status-tag {
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
  transition: background-color 0.2s;

  svg {
    font-size: 16px;
    width: 16px;
    height: 16px;
  }

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

  &.design {
    background-color: #e8f5e9;

    svg {
      color: #4caf50;
    }

    &:hover {
      background-color: #c8e6c9;
    }
  }

  &.code {
    background-color: #f3e5f5;

    svg {
      color: #9c27b0;
    }

    &:hover {
      background-color: #e1bee7;
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
  transition: all 0.2s;

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

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #ff9800;
}
</style>
