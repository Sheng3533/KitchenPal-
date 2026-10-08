import request from './request'

// 获取用户通知列表
export function getNotifications(userId) {
  return request.get(`/notification/${userId}`)
}

// 获取全部通知列表（管理端）
export function getNotificationList() {
  return request.get('/notification/list')
}

// 发送通知
export function sendNotification(data) {
  return request.post('/notification/send', data)
}

// 更新通知
export function updateNotification(id, data) {
  return request.put(`/notification/${id}`, data)
}

// 删除通知
export function deleteNotification(id) {
  return request.delete(`/notification/${id}`)
}

// 获取未读通知数量
export function getUnreadCount(userId) {
  return request.get(`/notification/unread/${userId}`)
}

// 标记单条通知为已读
export function markAsRead(id) {
  return request.post(`/notification/read/${id}`)
}

// 标记所有通知为已读
export function markAllAsRead(userId) {
  return request.post('/notification/read-all', { userId })
}
