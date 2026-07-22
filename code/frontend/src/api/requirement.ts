import request from './index'

export interface Requirement {
  id?: number
  name: string
  description: string
  status: string
  priority: string
  createdAt?: string
  updatedAt?: string
}

export interface RequirementQuery {
  keyword?: string
  status?: string
  page?: number
  size?: number
}

export const requirementApi = {
  list(params: RequirementQuery) {
    return request.get('/requirements', { params })
  },
  getById(id: number) {
    return request.get(`/requirements/${id}`)
  },
  create(data: Requirement) {
    return request.post('/requirements', data)
  },
  update(id: number, data: Requirement) {
    return request.put(`/requirements/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/requirements/${id}`)
  }
}
