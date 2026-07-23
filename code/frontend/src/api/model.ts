import request from './index'
import type { Requirement } from './requirement'

export interface Model {
  id?: string
  name: string
  code: string
  description: string
  requirements?: Requirement[]
  createdAt?: string
  updatedAt?: string
}

export interface ModelQuery {
  keyword?: string
  page?: number
  size?: number
}

export const modelApi = {
  list(params: ModelQuery) {
    return request.get('/models', { params })
  },
  getById(id: string) {
    return request.get(`/models/${id}`)
  },
  getRequirements(id: string) {
    return request.get(`/models/${id}/requirements`)
  },
  create(data: Model & { requirementIds?: string[] }) {
    return request.post('/models', data)
  },
  update(id: string, data: Model & { requirementIds?: string[] }) {
    return request.put(`/models/${id}`, data)
  },
  delete(id: string) {
    return request.delete(`/models/${id}`)
  }
}