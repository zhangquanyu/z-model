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

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const propertyApi = {
  list(modelId: string, params?: any) {
    return request.get(`/models/${modelId}/properties`, { params }) as unknown as Promise<PageResult<Property>>
  },
  listByRequirement(modelId: string, requirementId: string) {
    return request.get(`/models/${modelId}/properties/by-requirement`, { params: { requirementId } }) as unknown as Promise<Property[]>
  },
  getById(modelId: string, propertyId: string) {
    return request.get(`/models/${modelId}/properties/${propertyId}`) as unknown as Promise<Property>
  },
  create(modelId: string, data: Property) {
    return request.post(`/models/${modelId}/properties`, data) as unknown as Promise<Property>
  },
  update(modelId: string, propertyId: string, data: Property) {
    return request.put(`/models/${modelId}/properties/${propertyId}`, data) as unknown as Promise<Property>
  },
  delete(modelId: string, propertyId: string) {
    return request.delete(`/models/${modelId}/properties/${propertyId}`) as unknown as Promise<void>
  }
}