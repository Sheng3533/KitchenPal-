import request from './request'

export function getContentList() {
  return request.get('/content/list')
}

export function getContentDetail(id) {
  return request.get(`/content/${id}`)
}

export function getPublishedContent() {
  return request.get('/content/published')
}

export function getContentByType(type) {
  return request.get(`/content/type/${type}`)
}

export function createContent(data) {
  return request.post('/content', data)
}

export function updateContent(id, data) {
  return request.put(`/content/${id}`, data)
}

export function deleteContent(id) {
  return request.delete(`/content/${id}`)
}
