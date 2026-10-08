import request from './request'

export function getVideoList() {
  return request.get('/video/list')
}

export function getVideoDetail(id) {
  return request.get(`/video/${id}`)
}

export function getPublishedVideos() {
  return request.get('/video/published')
}

export function getVideosByCategory(category) {
  return request.get(`/video/category/${category}`)
}

export function createVideo(data) {
  return request.post('/video', data)
}

export function updateVideo(id, data) {
  return request.put(`/video/${id}`, data)
}

export function deleteVideo(id) {
  return request.delete(`/video/${id}`)
}
