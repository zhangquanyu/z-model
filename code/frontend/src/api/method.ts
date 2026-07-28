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

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const methodApi = {
  list(modelId: string, params?: any) {
    return request.get(`/models/${modelId}/methods`, { params }) as unknown as Promise<PageResult<Method>>
  },
  getById(modelId: string, methodId: string) {
    return request.get(`/models/${modelId}/methods/${methodId}`) as unknown as Promise<Method>
  },
  create(modelId: string, data: Method) {
    return request.post(`/models/${modelId}/methods`, data) as unknown as Promise<Method>
  },
  update(modelId: string, methodId: string, data: Method) {
    return request.put(`/models/${modelId}/methods/${methodId}`, data) as unknown as Promise<Method>
  },
  delete(modelId: string, methodId: string) {
    return request.delete(`/models/${modelId}/methods/${methodId}`) as unknown as Promise<void>
  }
}