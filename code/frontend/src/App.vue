<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import {
  LayoutDashboard,
  FileText,
  Box,
  Cog,
  Activity,
  Plus,
  ChevronLeft,
  ChevronRight
} from '@element-plus/icons-vue'
import { ref } from 'vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const menuItems = [
  { path: '/', name: '仪表盘', icon: LayoutDashboard },
  { path: '/requirements', name: '需求管理', icon: FileText },
  { path: '/models', name: '模型管理', icon: Box },
  { path: '/events', name: '事件流水', icon: Activity }
]

const isActive = (path: string) => {
  return route.path.startsWith(path)
}

const handleNavigation = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div class="app-container">
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <Cog class="logo-icon" />
          <span v-if="!collapsed" class="logo-text">Z-Model</span>
        </div>
      </div>
      
      <nav class="sidebar-menu">
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
          @click="handleNavigation(item.path)"
        >
          <component :is="item.icon" class="menu-icon" />
          <span v-if="!collapsed" class="menu-text">{{ item.name }}</span>
        </div>
      </nav>
      
      <div class="sidebar-footer">
        <button class="collapse-btn" @click="collapsed = !collapsed">
          <ChevronLeft v-if="!collapsed" />
          <ChevronRight v-else />
        </button>
      </div>
    </aside>
    
    <main class="main-content">
      <header class="header">
        <div class="header-title">
          <h1>{{ route.name === 'Dashboard' ? '仪表盘' : 
              route.name === 'RequirementList' ? '需求管理' :
              route.name === 'ModelList' ? '模型管理' :
              route.name === 'EventList' ? '事件流水' :
              route.name === 'ModelDetail' ? '模型详情' :
              route.name === 'PropertyList' ? '属性管理' :
              route.name === 'MethodList' ? '方法管理' :
              route.name === 'RequirementCreate' || route.name === 'RequirementEdit' ? '需求编辑' :
              route.name === 'ModelCreate' ? '模型创建' :
              route.name === 'EventCreate' || route.name === 'EventEdit' ? '事件编辑' : '页面' }}</h1>
        </div>
        <div class="header-actions">
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
          <router-link 
            v-if="route.path === '/events'" 
            to="/events/create"
            class="create-btn"
          >
            <Plus class="btn-icon" />
            <span>登记事件</span>
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
  font-size: 28px;
  color: var(--secondary-color);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.sidebar-menu {
  flex: 1;
  padding: 20px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.menu-item:hover {
  background-color: var(--sidebar-hover);
}

.menu-item.active {
  background-color: rgba(0, 212, 255, 0.2);
  border-left: 3px solid var(--secondary-color);
}

.menu-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.menu-text {
  font-size: 14px;
  font-weight: 500;
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
}

.content-wrapper {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}
</style>
