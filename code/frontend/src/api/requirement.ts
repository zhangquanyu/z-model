import request from './index'
import type { Model } from './model'

export interface Requirement {
  id?: string
  name: string
  code?: string
  description: string
  status: string
  priority: string
  requirementType?: string
  parentId?: string
  parentName?: string
  children?: Requirement[]
  createdAt?: string
  updatedAt?: string
}

export interface RequirementQuery {
  keyword?: string
  status?: string
  page?: number
  size?: number
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const requirementApi = {
  list(params: RequirementQuery) {
    return request.get('/requirements', { params }) as unknown as Promise<PageResult<Requirement>>
  },
  listMainRequirements(keyword?: string) {
    return request.get('/requirements/main', { params: { keyword } }) as unknown as Promise<Requirement[]>
  },
  getById(id: string) {
    return request.get(`/requirements/${id}`) as unknown as Promise<Requirement>
  },
  listSubRequirements(parentId: string) {
    return request.get(`/requirements/${parentId}/sub`) as unknown as Promise<Requirement[]>
  },
  getModelsByRequirement(id: string) {
    return request.get(`/requirements/${id}/models`) as unknown as Promise<Model[]>
  },
  create(data: Requirement) {
    return request.post('/requirements', data) as unknown as Promise<Requirement>
  },
  update(id: string, data: Requirement) {
    return request.put(`/requirements/${id}`, data) as unknown as Promise<Requirement>
  },
  delete(id: string) {
    return request.delete(`/requirements/${id}`) as unknown as Promise<void>
  }
}