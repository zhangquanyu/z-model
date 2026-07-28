import request from './index'

export interface BpmnProcess {
  id?: string
  name: string
  code: string
  description?: string
  bpmnXml?: string
  version?: number
  status?: string
  createdAt?: string
  updatedAt?: string
  versions?: BpmnProcessVersion[]
  nodeBindings?: NodeModelBinding[]
}

export interface BpmnProcessVersion {
  id?: string
  processId: string
  version: number
  bpmnXml: string
  changeNote?: string
  createdAt?: string
}

export interface NodeModelBinding {
  id?: string
  processId: string
  nodeId: string
  modelId: string
  modelName?: string
  modelCode?: string
  createdAt?: string
}

export interface BpmnProcessQuery {
  keyword?: string
  page?: number
  size?: number
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const processApi = {
  list(params: BpmnProcessQuery) {
    return request.get('/processes', { params }) as unknown as Promise<PageResult<BpmnProcess>>
  },
  getById(id: string) {
    return request.get(`/processes/${id}`) as unknown as Promise<BpmnProcess>
  },
  create(data: { name: string; code?: string; description?: string; bpmnXml?: string }) {
    return request.post('/processes', data) as unknown as Promise<BpmnProcess>
  },
  update(id: string, data: { name?: string; description?: string; bpmnXml?: string; status?: string; changeNote?: string }) {
    return request.put(`/processes/${id}`, data) as unknown as Promise<BpmnProcess>
  },
  delete(id: string) {
    return request.delete(`/processes/${id}`) as unknown as Promise<void>
  },
  getVersions(processId: string) {
    return request.get(`/processes/${processId}/versions`) as unknown as Promise<BpmnProcessVersion[]>
  },
  rollbackVersion(processId: string, version: number) {
    return request.post(`/processes/${processId}/rollback`, null, { params: { version } }) as unknown as Promise<BpmnProcess>
  },
  getNodeBindings(processId: string) {
    return request.get(`/processes/${processId}/bindings`) as unknown as Promise<NodeModelBinding[]>
  },
  getModelBindingsByNode(processId: string, nodeId: string) {
    return request.get(`/processes/${processId}/bindings/${nodeId}`) as unknown as Promise<NodeModelBinding[]>
  },
  bindNodeModel(processId: string, data: { nodeId: string; modelId: string }) {
    return request.post(`/processes/${processId}/bindings`, data) as unknown as Promise<NodeModelBinding>
  },
  unbindNodeModel(processId: string, nodeId: string) {
    return request.delete(`/processes/${processId}/bindings/${nodeId}`) as unknown as Promise<void>
  }
}
