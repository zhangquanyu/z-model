import request from './index'

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

export const methodApi = {
  list(modelId: string) {
    return request.get(`/models/${modelId}/methods`)
  },
  getById(modelId: string, methodId: string) {
    return request.get(`/models/${modelId}/methods/${methodId}`)
  },
  create(modelId: string, data: Method) {
    return request.post(`/models/${modelId}/methods`, data)
  },
  update(modelId: string, methodId: string, data: Method) {
    return request.put(`/models/${modelId}/methods/${methodId}`, data)
  },
  delete(modelId: string, methodId: string) {
    return request.delete(`/models/${modelId}/methods/${methodId}`)
  }
}