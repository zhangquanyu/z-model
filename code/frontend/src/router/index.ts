import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue')
  },
  {
    path: '/requirements',
    name: 'RequirementList',
    component: () => import('@/views/requirement/RequirementList.vue')
  },
  {
    path: '/requirements/create',
    name: 'RequirementCreate',
    component: () => import('@/views/requirement/RequirementForm.vue')
  },
  {
    path: '/requirements/:id/edit',
    name: 'RequirementEdit',
    component: () => import('@/views/requirement/RequirementForm.vue')
  },
  {
    path: '/models',
    name: 'ModelList',
    component: () => import('@/views/model/ModelList.vue')
  },
  {
    path: '/models/create',
    name: 'ModelCreate',
    component: () => import('@/views/model/ModelForm.vue')
  },
  {
    path: '/models/:id',
    name: 'ModelDetail',
    component: () => import('@/views/model/ModelDetail.vue')
  },
  {
    path: '/models/:id/properties',
    name: 'PropertyList',
    component: () => import('@/views/model/PropertyList.vue')
  },
  {
    path: '/models/:id/methods',
    name: 'MethodList',
    component: () => import('@/views/model/MethodList.vue')
  },
  {
    path: '/events',
    name: 'EventList',
    component: () => import('@/views/event/EventList.vue')
  },
  {
    path: '/events/create',
    name: 'EventCreate',
    component: () => import('@/views/event/EventForm.vue')
  },
  {
    path: '/events/:id/edit',
    name: 'EventEdit',
    component: () => import('@/views/event/EventForm.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
