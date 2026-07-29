import request from './index'
import type { Requirement } from './requirement'

export interface Model {
  id?: string
  name: string
  code: string
  description: string
  requirements?: Requirement[]
  methods?: Method[]
  createdAt?: string
  updatedAt?: string
}

export interface Method {
  id?: string
  modelId?: string
  modelName?: string
  requirementId?: string
  requirementName?: string
  parentRequirementId?: string
  parentRequirementName?: string
  name: string
  code?: string
  description: string
  inputParams?: string[]
  outputParams?: string[]
  createdAt?: string
  updatedAt?: string
}

export interface ModelQuery {
  keyword?: string
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

export const modelApi = {
  list(params: ModelQuery) {
    return request.get('/models', { params }) as unknown as Promise<PageResult<Model>>
  },
  listAll() {
    return request.get('/models/all') as unknown as Promise<Model[]>
  },
  getById(id: string) {
    return request.get(`/models/${id}`) as unknown as Promise<Model>
  },
  getRequirements(id: string) {
    return request.get(`/models/${id}/requirements`) as unknown as Promise<Requirement[]>
  },
  create(data: Model & { requirementIds?: string[] }) {
    return request.post('/models', data) as unknown as Promise<Model>
  },
  update(id: string, data: Model & { requirementIds?: string[] }) {
    return request.put(`/models/${id}`, data) as unknown as Promise<Model>
  },
  delete(id: string) {
    return request.delete(`/models/${id}`) as unknown as Promise<void>
  }
}