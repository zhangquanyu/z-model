import request from './index'

export interface Property {
  id?: number
  modelId?: number
  requirementId: number
  requirementName?: string
  name: string
  code: string
  type: string
  description: string
  nullable: boolean
  length: number
  createdAt?: string
  updatedAt?: string
}

export const propertyApi = {
  list(modelId: number) {
    return request.get(`/models/${modelId}/properties`)
  },
  getById(modelId: number, propertyId: number) {
    return request.get(`/models/${modelId}/properties/${propertyId}`)
  },
  create(modelId: number, data: Property) {
    return request.post(`/models/${modelId}/properties`, data)
  },
  update(modelId: number, propertyId: number, data: Property) {
    return request.put(`/models/${modelId}/properties/${propertyId}`, data)
  },
  delete(modelId: number, propertyId: number) {
    return request.delete(`/models/${modelId}/properties/${propertyId}`)
  }
}
