<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import {
  Monitor,
  Document,
  Box,
  Setting,
  Plus,
  ArrowLeft,
  ArrowRight,
  FolderOpened,
  Share,
  Connection,
  Files,
  Operation
} from '@element-plus/icons-vue'
import { ref, computed } from 'vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const menuItems = [
  { path: '/', name: '仪表盘', icon: Monitor },
  { path: '/processes', name: '业务流程', icon: Share },
  { 
    name: '业务处理', 
    icon: FolderOpened,
    children: [
      { path: '/requirements', name: '需求管理', icon: Document },
      { path: '/models', name: '模型管理', icon: Box },
      { path: '/orchestrations', name: '业务编排', icon: Connection }
    ]
  },
  { 
    name: '技术处理', 
    icon: Operation,
    children: [
      { path: '/physical-models', name: '物理模型', icon: Files },
      { path: '/functional-orchestrations', name: '功能编排', icon: Share }
    ]
  }
]

const activeMenu = computed(() => {
  return route.path
})

const handleMenuSelect = (index: string) => {
  router.push(index)
}
</script>

<template>
  <div class="app-container">
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <Setting class="logo-icon" />
          <span v-if="!collapsed" class="logo-text">Z-Model</span>
        </div>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        background-color="#1e3a5f"
        text-color="#ffffff"
        active-text-color="#00d4ff"
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <template v-for="item in menuItems" :key="item.path || item.name">
          <el-menu-item v-if="!item.children" :index="item.path!">
            <component :is="item.icon" class="menu-icon" />
            <template #title>{{ item.name }}</template>
          </el-menu-item>
          <el-sub-menu v-else :index="item.name">
            <template #title>
              <component :is="item.icon" class="menu-icon" />
              <span>{{ item.name }}</span>
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.path"
              :index="child.path"
            >
              <component :is="child.icon" class="menu-icon" />
              <template #title>{{ child.name }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
      
      <div class="sidebar-footer">
        <button class="collapse-btn" @click="collapsed = !collapsed">
          <ArrowLeft v-if="!collapsed" />
          <ArrowRight v-else />
        </button>
      </div>
    </aside>
    
    <main class="main-content">
      <header class="header">
        <div class="header-title">
          <h1>{{ 
            route.name === 'Dashboard' ? '仪表盘' : 
            route.name === 'ProcessList' ? '业务流程' :
            route.name === 'ProcessDetail' ? '流程详情' :
            route.name === 'ProcessDesign' ? '流程设计器' :
            route.name === 'ProcessCreate' || route.name === 'ProcessEdit' ? '流程编辑' :
            route.name === 'RequirementList' ? '需求管理' :
            route.name === 'ModelList' ? '模型管理' :
            route.name === 'ModelDetail' ? '模型详情' :
            route.name === 'PropertyList' ? '属性管理' :
            route.name === 'MethodList' ? '方法管理' :
            route.name === 'RequirementCreate' || route.name === 'RequirementEdit' ? '需求编辑' :
            route.name === 'ModelCreate' ? '模型创建' :
            route.name === 'OrchestrationList' ? '业务编排' :
            route.name === 'OrchestrationDetail' ? '编排详情' :
            route.name === 'OrchestrationDesign' ? '编排设计器' :
            route.name === 'OrchestrationEdit' ? '编排编辑' :
            route.name === 'PhysicalModelList' ? '物理模型' :
            route.name === 'PhysicalModelDetail' ? '物理模型详情' :
            route.name === 'PhysicalPropertyList' ? '扩展属性管理' :
            route.name === 'PhysicalMethodList' ? '扩展方法管理' :
            route.name === 'FunctionalOrchestrationList' ? '功能编排' :
            route.name === 'FunctionalOrchestrationDetail' ? '功能编排详情' :
            route.name === 'FunctionalOrchestrationDesign' ? '功能编排设计器' :
            '页面' 
          }}</h1>
        </div>
        <div class="header-actions">
          <router-link 
            v-if="route.path === '/processes'" 
            to="/processes/create"
            class="create-btn"
          >
            <Plus class="btn-icon" />
            <span>新建流程</span>
          </router-link>
          <router-link 
            v-if="route.path === '/requirements'" 
            to="/requirements/create"
            class="create-btn"
          >
            <Plus class="btn-icon" />
            <span>新建需求</span>
          </router-link>
          <router-link 
            v-if="route.path === '/models'" 
            to="/models/create"
            class="create-btn"
          >
            <Plus class="btn-icon" />
            <span>新建模型</span>
          </router-link>
        </div>
      </header>
      
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style lang="scss">
:root {
  --primary-color: #1e3a5f;
  --secondary-color: #00d4ff;
  --bg-color: #f5f7fa;
  --sidebar-bg: #1e3a5f;
  --sidebar-hover: #2d4a6f;
  --text-color: #333;
  --text-light: #999;
}

/* 强制缩小所有图标 */
.sidebar svg {
  max-width: 24px !important;
  max-height: 24px !important;
  width: 24px !important;
  height: 24px !important;
}

.sidebar .menu-icon {
  font-size: 16px !important;
  width: 16px !important;
  height: 16px !important;
  color: inherit;
  flex-shrink: 0;
  display: inline-flex !important;
  
  svg {
    width: 16px !important;
    height: 16px !important;
  }
}

.sidebar .logo-icon svg {
  max-width: 24px !important;
  max-height: 24px !important;
  width: 24px !important;
  height: 24px !important;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.app-container {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 240px;
  background-color: var(--sidebar-bg);
  color: white;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  height: 100vh;
  left: 0;
  top: 0;
  z-index: 100;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 20px !important;
  color: var(--secondary-color);
  width: 24px !important;
  height: 24px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  
  & svg {
    width: 24px !important;
    height: 24px !important;
  }
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

/* Element Plus 菜单样式覆盖 */
.sidebar :deep(.el-menu-item) {
  height: 48px !important;
  line-height: 48px !important;
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
}

.sidebar :deep(.el-menu-item .menu-icon) {
  margin-right: 0 !important;
}

.sidebar :deep(.el-sub-menu__title) {
  height: 48px !important;
  line-height: 48px !important;
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
}

.sidebar :deep(.el-sub-menu__title .menu-icon) {
  margin-right: 0 !important;
}

.sidebar :deep(.el-sub-menu .el-menu-item) {
  height: 44px !important;
  line-height: 44px !important;
  padding-left: 60px !important;
}

.sidebar :deep(.el-menu-item:hover),
.sidebar :deep(.el-sub-menu__title:hover) {
  background-color: rgba(0, 212, 255, 0.1) !important;
}

.sidebar :deep(.el-menu-item.is-active) {
  background-color: rgba(0, 212, 255, 0.2) !important;
}

/* 折叠状态下弹出菜单的图标样式 */
.el-menu--popup {
  .menu-icon {
    font-size: 16px !important;
    width: 16px !important;
    height: 16px !important;
    flex-shrink: 0;
    display: inline-flex !important;
  }
  
  .menu-icon svg {
    width: 16px !important;
    height: 16px !important;
  }
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.collapse-btn {
  width: 100%;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.1);
  border: none;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.collapse-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.main-content {
  flex: 1;
  margin-left: 240px;
  transition: margin-left 0.3s ease;
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed + .main-content {
  margin-left: 64px;
}

.header {
  background-color: white;
  padding: 20px 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title h1 {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: var(--primary-color);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.create-btn:hover {
  background-color: #2d4a6f;
  transform: translateY(-1px);
}

.btn-icon {
  font-size: 16px;
  width: 16px;
  height: 16px;
  
  & svg {
    width: 16px !important;
    height: 16px !important;
  }
}

.content-wrapper {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}
</style>
