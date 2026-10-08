import request from './request'

// 用户登录
export function login(data) {
  return request.post('/user/login', data)
}

// 用户注册
export function register(data) {
  return request.post('/user/register', data)
}

// 获取用户信息
export function getUserInfo(id) {
  return request.get(`/user/${id}`)
}

// 更新用户信息
export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

// 获取用户列表
export function getUserList() {
  return request.get('/user/list')
}

// 获取用户详情
export function getUserDetail(id) {
  return request.get(`/user/${id}`)
}

// 创建用户
export function createUser(data) {
  return request.post('/user/register', data)
}

// 删除用户
export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}

// 切换用户状态
export function toggleUserStatus(id) {
  return request.put(`/user/${id}/status`)
}

// 修改密码
export function changePassword(data) {
  return request.post('/user/change-password', data)
}
