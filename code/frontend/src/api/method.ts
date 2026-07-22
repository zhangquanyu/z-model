import request from './index'

export interface Method {
  id?: number
  modelId?: number
  requirementId: number
  requirementName?: string
  name: string
  code: string
  description: string
  inputParams?: number[]
  outputParams?: number[]
  createdAt?: string
  updatedAt?: string
}

export const methodApi = {
  list(modelId: number) {
    return request.get(`/models/${modelId}/methods`)
  },
  getById(modelId: number, methodId: number) {
    return request.get(`/models/${modelId}/methods/${methodId}`)
  },
  create(modelId: number, data: Method) {
    return request.post(`/models/${modelId}/methods`, data)
  },
  update(modelId: number, methodId: number, data: Method) {
    return request.put(`/models/${modelId}/methods/${methodId}`, data)
  },
  delete(modelId: number, methodId: number) {
    return request.delete(`/models/${modelId}/methods/${methodId}`)
  }
}
