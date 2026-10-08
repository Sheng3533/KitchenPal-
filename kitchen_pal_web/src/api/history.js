import request from './request'

// 获取用户烹饪历史
export function getCookingHistory(userId) {
  return request.get(`/history/${userId}`)
}

// 获取全部烹饪历史
export function getAllCookingHistory() {
  return request.get('/history/list')
}

// 删除烹饪历史
export function deleteCookingHistory(id) {
  return request.delete(`/history/${id}`)
}

// 添加烹饪历史
export function addCookingHistory(data) {
  return request.post('/history/add', data)
}
