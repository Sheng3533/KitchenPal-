import request from './request'

export function getChefList() {
  return request.get('/chef/list')
}

export function getChefDetail(id) {
  return request.get(`/chef/${id}`)
}

export function createChef(data) {
  return request.post('/chef', data)
}

export function updateChef(id, data) {
  return request.put(`/chef/${id}`, data)
}

export function deleteChef(id) {
  return request.delete(`/chef/${id}`)
}

export function chatWithChef(id, data) {
  return request.post(`/chef/${id}/chat`, data)
}
