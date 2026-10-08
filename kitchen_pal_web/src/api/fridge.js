import request from './request'

// 冰箱食材列表（管理端可按 userId 查看）
export function getFridgeItems(userId) {
  return request.get(`/fridge/${userId}`)
}

// 临期食材
export function getExpiringItems(userId, days = 3) {
  return request.get(`/fridge/${userId}/expiring`, { params: { days } })
}

// 新增食材
export function addFridgeItem(data) {
  return request.post('/fridge', data)
}

// 更新食材
export function updateFridgeItem(id, data) {
  return request.put(`/fridge/${id}`, data)
}

// 删除食材
export function deleteFridgeItem(id) {
  return request.delete(`/fridge/${id}`)
}
