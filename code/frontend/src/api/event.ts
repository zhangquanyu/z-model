import request from './index'

export interface Event {
  id?: number
  externalFlowNo1: string
  externalFlowNo2?: string
  pointBrandCode?: string
  sceneCode?: string
  mainOrderNo?: string
  subOrderNo?: string
  eventTime?: string
  partnerCode?: string
  memberCardNo?: string
  salesChannel1?: string
  salesChannel2?: string
  entryFlag?: number
  externalFlowNo3?: string
  businessTag?: string
  eventType?: string
  eventAmount?: number
  pfrId?: string
  operator?: string
  remark?: string
  status?: string
  createdAt?: string
  updatedAt?: string
}

export interface EventQuery {
  externalFlowNo1?: string
  memberCardNo?: string
  eventType?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}

export const eventApi = {
  list(params: EventQuery) {
    return request.get('/events', { params })
  },
  getById(id: number) {
    return request.get(`/events/${id}`)
  },
  create(data: Event) {
    return request.post('/events', data)
  },
  update(id: number, data: Event) {
    return request.put(`/events/${id}`, data)
  },
  updateStatus(id: number, status: string) {
    return request.put(`/events/${id}/status`, { status })
  },
  validate(data: { externalFlowNo1: string }) {
    return request.post('/events/validate', data)
  },
  calculateTotal(params: { memberCardNo?: string; eventType?: string; startTime?: string; endTime?: string }) {
    return request.get('/events/total', { params })
  },
  searchByFlowNo(externalFlowNo1: string, params?: { page?: number; size?: number }) {
    return request.get('/events/search/flowNo', { params: { externalFlowNo1, ...params } })
  },
  searchByMemberCard(memberCardNo: string, params?: { page?: number; size?: number }) {
    return request.get('/events/search/memberCard', { params: { memberCardNo, ...params } })
  },
  checkReversible(id: number) {
    return request.get(`/events/${id}/reversible`)
  },
  findOriginalEvents(id: number, params?: { page?: number; size?: number }) {
    return request.get(`/events/${id}/original`, { params })
  }
}
