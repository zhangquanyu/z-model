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
    path: '/requirements/:id',
    name: 'RequirementDetail',
    component: () => import('@/views/requirement/RequirementDetail.vue')
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
    path: '/models/:id/edit',
    name: 'ModelEdit',
    component: () => import('@/views/model/ModelForm.vue')
  },
  {
    path: '/models/:id/properties',
    name: 'PropertyList',
    component: () => import('@/views/model/PropertyList.vue')
  },
  {
    path: '/models/:id/properties/:propertyId',
    name: 'PropertyDetail',
    component: () => import('@/views/model/PropertyDetail.vue')
  },
  {
    path: '/models/:id/properties/create',
    name: 'PropertyCreate',
    component: () => import('@/views/model/PropertyForm.vue')
  },
  {
    path: '/models/:id/properties/:propertyId/edit',
    name: 'PropertyEdit',
    component: () => import('@/views/model/PropertyForm.vue')
  },
  {
    path: '/models/:id/methods',
    name: 'MethodList',
    component: () => import('@/views/model/MethodList.vue')
  },
  {
    path: '/models/:id/methods/:methodId',
    name: 'MethodDetail',
    component: () => import('@/views/model/MethodDetail.vue')
  },
  {
    path: '/models/:id/methods/create',
    name: 'MethodCreate',
    component: () => import('@/views/model/MethodForm.vue')
  },
  {
    path: '/models/:id/methods/:methodId/edit',
    name: 'MethodEdit',
    component: () => import('@/views/model/MethodForm.vue')
  },
  {
    path: '/processes',
    name: 'ProcessList',
    component: () => import('@/views/process/ProcessList.vue')
  },
  {
    path: '/processes/create',
    name: 'ProcessCreate',
    component: () => import('@/views/process/ProcessForm.vue')
  },
  {
    path: '/processes/:id',
    name: 'ProcessDetail',
    component: () => import('@/views/process/ProcessDetail.vue')
  },
  {
    path: '/processes/:id/edit',
    name: 'ProcessEdit',
    component: () => import('@/views/process/ProcessForm.vue')
  },
  {
    path: '/processes/:id/design',
    name: 'ProcessDesign',
    component: () => import('@/views/process/ProcessDesigner.vue')
  },
  ]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
