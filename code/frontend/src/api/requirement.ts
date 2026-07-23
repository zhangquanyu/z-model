import request from './index'

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

export const requirementApi = {
  list(params: RequirementQuery) {
    return request.get('/requirements', { params })
  },
  listMainRequirements(keyword?: string) {
    return request.get('/requirements/main', { params: { keyword } })
  },
  getById(id: string) {
    return request.get(`/requirements/${id}`)
  },
  listSubRequirements(parentId: string) {
    return request.get(`/requirements/${parentId}/sub`)
  },
  create(data: Requirement) {
    return request.post('/requirements', data)
  },
  update(id: string, data: Requirement) {
    return request.put(`/requirements/${id}`, data)
  },
  delete(id: string) {
    return request.delete(`/requirements/${id}`)
  }
}