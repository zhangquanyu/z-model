import request from './index'

export interface PhysicalModel {
  id?: string
  modelId: string
  modelName?: string
  name: string
  code?: string
  description?: string
  tableName?: string
  status?: string
  properties?: PhysicalProperty[]
  methods?: PhysicalMethod[]
  createdAt?: string
  updatedAt?: string
}

export interface PhysicalProperty {
  id?: string
  physicalModelId: string
  sourcePropertyId?: string
  sourcePropertyName?: string
  sourceMethodId?: string
  sourceMethodName?: string
  name: string
  code?: string
  dataType?: string
  dbType?: string
  dbLength?: number
  dbPrecision?: number
  dbScale?: number
  nullable?: boolean
  isPrimaryKey?: boolean
  isIndex?: boolean
  defaultValue?: string
  description?: string
}

export interface PhysicalMethod {
  id?: string
  physicalModelId: string
  sourceMethodId: string
  sourceMethodName?: string
  name?: string
  code?: string
  methodType?: string
  description?: string
  sqlTemplate?: string
  params?: PhysicalMethodParam[]
}

export interface PhysicalMethodParam {
  id?: string
  physicalMethodId: string
  physicalPropertyId?: string
  paramType?: string
  sortOrder?: number
}

export interface GenerateSQLResponse {
  physicalModelId?: string
  tableName?: string
  createTableSQL?: string
  methodSQLs?: MethodSQL[]
  completeSQL?: string
}

export interface MethodSQL {
  id?: string
  name?: string
  code?: string
  methodType?: string
  sql?: string
}

export interface PhysicalModelQuery {
  keyword?: string
  page?: number
  size?: number
}

export const physicalModelApi = {
  list(params: PhysicalModelQuery) {
    return request.get('/physical-models', { params }) as unknown as Promise<any>
  },
  listAll() {
    return request.get('/physical-models', { params: { page: 0, size: 9999 } }).then((res: any) => res.content || []) as unknown as Promise<PhysicalModel[]>
  },
  getById(id: string) {
    return request.get(`/physical-models/${id}`) as unknown as Promise<PhysicalModel>
  },
  getByModelId(modelId: string) {
    return request.get(`/physical-models/model/${modelId}`) as unknown as Promise<PhysicalModel[]>
  },
  create(data: PhysicalModel) {
    return request.post('/physical-models', data) as unknown as Promise<PhysicalModel>
  },
  update(id: string, data: PhysicalModel) {
    return request.put(`/physical-models/${id}`, data) as unknown as Promise<PhysicalModel>
  },
  delete(id: string) {
    return request.delete(`/physical-models/${id}`) as unknown as Promise<void>
  },
  generateSQL(id: string) {
    return request.get(`/physical-models/${id}/generate-sql`) as unknown as Promise<GenerateSQLResponse>
  }
}

export const physicalPropertyApi = {
  listByPhysicalModelId(physicalModelId: string) {
    return request.get(`/physical-properties/physical-model/${physicalModelId}`) as unknown as Promise<PhysicalProperty[]>
  },
  create(data: PhysicalProperty) {
    return request.post('/physical-properties', data) as unknown as Promise<PhysicalProperty>
  },
  update(id: string, data: PhysicalProperty) {
    return request.put(`/physical-properties/${id}`, data) as unknown as Promise<PhysicalProperty>
  },
  delete(id: string) {
    return request.delete(`/physical-properties/${id}`) as unknown as Promise<void>
  },
  syncFromSource(id: string) {
    return request.post(`/physical-properties/${id}/sync`) as unknown as Promise<PhysicalProperty>
  }
}

export const physicalMethodApi = {
  listByPhysicalModelId(physicalModelId: string) {
    return request.get(`/physical-methods/physical-model/${physicalModelId}`) as unknown as Promise<PhysicalMethod[]>
  },
  create(data: PhysicalMethod) {
    return request.post('/physical-methods', data) as unknown as Promise<PhysicalMethod>
  },
  update(id: string, data: PhysicalMethod) {
    return request.put(`/physical-methods/${id}`, data) as unknown as Promise<PhysicalMethod>
  },
  delete(id: string) {
    return request.delete(`/physical-methods/${id}`) as unknown as Promise<void>
  }
}
