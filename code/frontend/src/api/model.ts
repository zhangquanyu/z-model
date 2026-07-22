import request from './index'

export interface Model {
  id?: number
  name: string
  code: string
  description: string
  requirements?: Requirement[]
  createdAt?: string
  updatedAt?: string
}

export interface Requirement {
  id: number
  name: string
  description: string
  status: string
  priority: string
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
  getById(id: number) {
    return request.get(`/models/${id}`)
  },
  create(data: Model & { requirementIds?: number[] }) {
    return request.post('/models', data)
  },
  update(id: number, data: Model & { requirementIds?: number[] }) {
    return request.put(`/models/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/models/${id}`)
  }
}
