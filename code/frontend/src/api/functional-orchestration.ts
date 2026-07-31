import request from './index'

export interface FunctionalOrchestration {
  id?: string
  orchestrationId: string
  name: string
  code?: string
  description?: string
  status?: string
  nodes?: FoNodeDTO[]
  createdAt?: string
  updatedAt?: string
}

export interface FoNodeDTO {
  id?: string
  nodeType: string
  label?: string
  sortOrder?: number
  x?: number
  y?: number
  methods?: FoNodeMethodDTO[]
  nodeConfig?: FoNodeConfigDTO
}

export interface FoNodeMethodDTO {
  id?: string
  methodId?: string
  physicalModelId?: string
  sortOrder?: number
}

export interface FoNodeConfigDTO {
  configKey?: string
  configValue?: string
}

export interface GeneratedCodeResponse {
  orchestrationId?: string
  entityCode?: string
  mapperCode?: string
  serviceInterfaceCode?: string
  serviceCode?: string
  controllerCode?: string
  orchestrationCode?: string
  generatedTime?: string
}

export interface FunctionalOrchestrationQuery {
  keyword?: string
  page?: number
  size?: number
}

export const functionalOrchestrationApi = {
  list(params: FunctionalOrchestrationQuery) {
    return request.get('/functional-orchestrations', { params }) as unknown as Promise<any>
  },
  getById(id: string) {
    return request.get(`/functional-orchestrations/${id}`) as unknown as Promise<FunctionalOrchestration>
  },
  getByOrchestrationId(orchestrationId: string) {
    return request.get(`/functional-orchestrations/orchestration/${orchestrationId}`) as unknown as Promise<FunctionalOrchestration>
  },
  create(data: FunctionalOrchestration) {
    return request.post('/functional-orchestrations', data) as unknown as Promise<FunctionalOrchestration>
  },
  update(id: string, data: FunctionalOrchestration) {
    return request.put(`/functional-orchestrations/${id}`, data) as unknown as Promise<FunctionalOrchestration>
  },
  delete(id: string) {
    return request.delete(`/functional-orchestrations/${id}`) as unknown as Promise<void>
  },
  saveNodes(id: string, nodes: FoNodeDTO[]) {
    return request.put(`/functional-orchestrations/${id}/nodes`, nodes) as unknown as Promise<FunctionalOrchestration>
  },
  generateCode(id: string) {
    return request.get(`/functional-orchestrations/${id}/generate-code`) as unknown as Promise<GeneratedCodeResponse>
  },
  generateCodeByOrchestrationId(orchestrationId: string) {
    return request.get(`/functional-orchestrations/generate-by-orchestration/${orchestrationId}`) as unknown as Promise<GeneratedCodeResponse>
  }
}
