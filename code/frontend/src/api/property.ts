import request from './index'

export interface Property {
  id?: string
  modelId?: string
  modelName?: string
  requirementId?: string
  requirementName?: string
  parentRequirementId?: string
  parentRequirementName?: string
  name: string
  code?: string
  dataType: string
  description: string
  required?: boolean
  defaultValue?: string
  createdAt?: string
  updatedAt?: string
}

export const propertyApi = {
  list(modelId: string, params?: any) {
    return request.get(`/models/${modelId}/properties`, { params })
  },
  getById(modelId: string, propertyId: string) {
    return request.get(`/models/${modelId}/properties/${propertyId}`)
  },
  create(modelId: string, data: Property) {
    return request.post(`/models/${modelId}/properties`, data)
  },
  update(modelId: string, propertyId: string, data: Property) {
    return request.put(`/models/${modelId}/properties/${propertyId}`, data)
  },
  delete(modelId: string, propertyId: string) {
    return request.delete(`/models/${modelId}/properties/${propertyId}`)
  }
}