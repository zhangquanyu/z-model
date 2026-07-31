<script setup lang="ts">import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { physicalModelApi, type PhysicalModel } from '@/api/physical-model';
import { modelApi } from '@/api/model';
import { Search, Edit, Delete, View, Refresh, Setting, Files, Plus } from '@element-plus/icons-vue';
const router = useRouter();
const models = ref<PhysicalModel[]>([]);
const businessModels = ref<any[]>([]);
const total = ref(0);
const page = ref(0);
const size = ref(10);
const searchName = ref('');
const selectedBusinessModelId = ref('');
const showCreateDialog = ref(false);
const creatingModel = ref<PhysicalModel>({
 modelId: '',
 name: '',
 code: '',
 tableName: '',
 description: ''
});
const loadBusinessModels = async () => {
 try {
 const res = await modelApi.listAll();
 businessModels.value = res || [];
 }
 catch (error) {
 console.error('Failed to load business models:', error);
 }
};
const loadModels = async () => {
 try {
 const params: any = { page: page.value, size: size.value };
 if (searchName.value) {
 params.keyword = searchName.value;
 }
 if (selectedBusinessModelId.value) {
 params.modelId = selectedBusinessModelId.value;
 const res = await physicalModelApi.getByModelId(selectedBusinessModelId.value);
 models.value = res || [];
 total.value = models.value.length;
 return;
 }
 const res = await physicalModelApi.list(params);
 models.value = res.content || [];
 total.value = res.totalElements || 0;
 }
 catch (error) {
 console.error('Failed to load physical models:', error);
 }
};
const handleSearch = () => {
 page.value = 0;
 loadModels();
};
const handlePageChange = (newPage: number) => {
 page.value = newPage;
 loadModels();
};
const handleView = (id: string) => {
 router.push(`/physical-models/${id}`);
};
const handleEdit = (id: string) => {
 router.push(`/physical-models/${id}/edit`);
};
const handleDelete = async (id: string) => {
 if (confirm('确定要删除这个物理模型吗？')) {
 try {
 await physicalModelApi.delete(id);
 loadModels();
 }
 catch (error) {
 console.error('Failed to delete physical model:', error);
 }
 }
};
const handleManageProperties = (id: string) => {
 router.push(`/physical-models/${id}/properties`);
};
const handleManageMethods = (id: string) => {
 router.push(`/physical-models/${id}/methods`);
};
const handleRefresh = () => {
 searchName.value = '';
 selectedBusinessModelId.value = '';
 page.value = 0;
 loadModels();
};
const handleCreate = () => {
 creatingModel.value = { modelId: '', name: '', code: '', tableName: '', description: '' };
 showCreateDialog.value = true;
};
const handleCreateSubmit = async () => {
 try {
 await physicalModelApi.create(creatingModel.value);
 showCreateDialog.value = false;
 loadModels();
 }
 catch (error) {
 console.error('Failed to create physical model:', error);
 }
};
const getModelName = (modelId: string) => {
 const model = businessModels.value.find(m => m.id === modelId);
 return model?.name || '-';
};
onMounted(() => {
 loadBusinessModels();
 loadModels();
});
</script>

<template>
  <div class="physical-model-list">
    <div class="header">
      <h2 class="page-title">物理模型</h2>
      <button class="create-btn" @click="handleCreate">
        <Plus />
        新建物理模型
      </button>
    </div>

    <div class="search-bar">
      <div class="search-input-wrapper">
        <Search class="search-icon" />
        <input
          v-model="searchName"
          type="text"
          placeholder="搜索物理模型名称..."
          @keyup.enter="handleSearch"
        />
      </div>
      <select v-model="selectedBusinessModelId" class="filter-select">
        <option value="">选择业务模型</option>
        <option v-for="bm in businessModels" :key="bm.id" :value="bm.id">
          {{ bm.name }}
        </option>
      </select>
      <button class="search-btn" @click="handleSearch">搜索</button>
      <button class="refresh-btn" @click="handleRefresh">
        <Refresh />
      </button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>物理模型名称</th>
            <th>编号</th>
            <th>关联业务模型</th>
            <th>数据库表名</th>
            <th>状态</th>
            <th>扩展属性</th>
            <th>扩展方法</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="model in models" :key="model.id">
            <td>{{ model.name }}</td>
            <td>{{ model.code || '-' }}</td>
            <td>{{ getModelName(model.modelId) }}</td>
            <td>{{ model.tableName || '-' }}</td>
            <td>
              <span :class="['status-tag', model.status?.toLowerCase() === 'completed' ? 'completed' : model.status?.toLowerCase() === 'designing' ? 'designing' : 'draft']">
                {{ model.status === 'COMPLETED' ? '已完成' : model.status === 'DESIGNING' ? '设计中' : '草稿' }}
              </span>
            </td>
            <td>{{ model.properties?.length || 0 }}</td>
            <td>{{ model.methods?.length || 0 }}</td>
            <td>{{ model.createdAt?.slice(0, 10) }}</td>
            <td class="actions">
              <el-tooltip content="查看" placement="top">
                <button class="action-btn view" @click="handleView(model.id!)">
                  <View />
                </button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <button class="action-btn edit" @click="handleEdit(model.id!)">
                  <Edit />
                </button>
              </el-tooltip>
              <el-tooltip content="属性配置" placement="top">
                <button class="action-btn property" @click="handleManageProperties(model.id!)">
                  <Setting />
                </button>
              </el-tooltip>
              <el-tooltip content="方法配置" placement="top">
                <button class="action-btn method" @click="handleManageMethods(model.id!)">
                  <Files />
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="action-btn delete" @click="handleDelete(model.id!)">
                  <Delete />
                </button>
              </el-tooltip>
            </td>
          </tr>
          <tr v-if="models.length === 0">
            <td colspan="9" class="empty-row">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="!selectedBusinessModelId">
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
    <el-dialog v-model="showCreateDialog" title="新建物理模型" width="500px">
      <el-form :model="creatingModel" label-width="120px">
        <el-form-item label="业务模型" required>
          <el-select v-model="creatingModel.modelId" placeholder="请选择业务模型" style="width: 100%">
            <el-option v-for="bm in businessModels" :key="bm.id" :label="bm.name" :value="bm.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="物理模型名称" required>
          <el-input v-model="creatingModel.name" placeholder="请输入物理模型名称" />
        </el-form-item>
        <el-form-item label="编号">
          <el-input v-model="creatingModel.code" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="数据库表名">
          <el-input v-model="creatingModel.tableName" placeholder="请输入数据库表名" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="creatingModel.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
.physical-model-list {
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

.filter-select {
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  background-color: white;
  font-size: 14px;
  cursor: pointer;
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

  &.method {
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
</style>
