import request from './index'
import type { Requirement } from './requirement'

export interface OrchestrationSummary {
  id?: string
  name: string
  code?: string
  description?: string
  status?: string
  version?: number
  nodeCount?: number
  methodCount?: number
  createdAt?: string
  updatedAt?: string
}

export interface OrchestrationNodeMethod {
  id?: string
  nodeId?: string
  methodId: string
  methodName?: string
  methodCode?: string
  modelId?: string
  modelName?: string
  requirementId?: string
  requirementName?: string
  requirementCode?: string
  sortOrder?: number
  createdAt?: string
}

export interface OrchestrationNode {
  id?: string
  orchestrationId?: string
  parentId?: string
  nodeType: string
  nodeName?: string
  description?: string
  sortOrder?: number
  loopCount?: number
  width?: number
  methods?: OrchestrationNodeMethod[]
  children?: OrchestrationNode[]
  createdAt?: string
}

export interface Orchestration {
  id?: string
  name: string
  code?: string
  description?: string
  status?: string
  version?: number
  nodes?: OrchestrationNode[]
  requirements?: Requirement[]
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

export interface OrchestrationCreateRequest {
  name: string
  code?: string
  description?: string
}

export interface OrchestrationUpdateRequest {
  name?: string
  description?: string
  status?: string
}

export interface OrchestrationNodeRequest {
  nodeType?: string
  nodeName?: string
  description?: string
  sortOrder?: number
  loopCount?: number
}

export interface OrchestrationNodeMethodRequest {
  methodId: string
  requirementId?: string
  parentRequirementId?: string
  newRequirementName?: string
  newRequirementDescription?: string
  sortOrder?: number
}

export const orchestrationApi = {
  list(keyword?: string, page?: number, size?: number) {
    return request.get('/orchestrations', { params: { keyword, page, size } }) as unknown as Promise<PageResult<OrchestrationSummary>>
  },
  getById(id: string) {
    return request.get(`/orchestrations/${id}`) as unknown as Promise<Orchestration>
  },
  create(data: OrchestrationCreateRequest) {
    return request.post('/orchestrations', data) as unknown as Promise<Orchestration>
  },
  update(id: string, data: OrchestrationUpdateRequest) {
    return request.put(`/orchestrations/${id}`, data) as unknown as Promise<Orchestration>
  },
  delete(id: string) {
    return request.delete(`/orchestrations/${id}`) as unknown as Promise<void>
  },
  addNode(orchestrationId: string, data: OrchestrationNodeRequest) {
    return request.post(`/orchestrations/${orchestrationId}/nodes`, data) as unknown as Promise<OrchestrationNode>
  },
  updateNode(orchestrationId: string, nodeId: string, data: OrchestrationNodeRequest) {
    return request.put(`/orchestrations/${orchestrationId}/nodes/${nodeId}`, data) as unknown as Promise<OrchestrationNode>
  },
  deleteNode(orchestrationId: string, nodeId: string) {
    return request.delete(`/orchestrations/${orchestrationId}/nodes/${nodeId}`) as unknown as Promise<void>
  },
  addNodeMethod(orchestrationId: string, nodeId: string, data: OrchestrationNodeMethodRequest) {
    return request.post(`/orchestrations/${orchestrationId}/nodes/${nodeId}/methods`, data) as unknown as Promise<OrchestrationNodeMethod>
  },
  removeNodeMethod(orchestrationId: string, nodeId: string, methodId: string) {
    return request.delete(`/orchestrations/${orchestrationId}/nodes/${nodeId}/methods/${methodId}`) as unknown as Promise<void>
  },
  updateNodeSort(orchestrationId: string, nodeIds: string[]) {
    return request.put(`/orchestrations/${orchestrationId}/nodes/sort`, nodeIds) as unknown as Promise<void>
  },
  saveDesign(id: string, data: any) {
    return request.put(`/orchestrations/${id}/design`, data) as unknown as Promise<Orchestration>
  },
  getRequirements(orchestrationId: string) {
    return request.get(`/orchestrations/${orchestrationId}/requirements`) as unknown as Promise<Requirement[]>
  }
}
