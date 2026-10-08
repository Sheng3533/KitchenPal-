import request from './request'

// 菜谱评论列表（公开，无需登录）
export function getComments(recipeId) {
  return request.get(`/comment/recipe/${recipeId}`)
}

// 发表评论（需登录，Web 管理端也可作为运营账号发表）
export function addComment(data) {
  return request.post('/comment', data)
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/comment/${id}`)
}

// 对菜谱评分（需登录）
export function rateRecipe(id, data) {
  return request.post(`/recipe/${id}/rate`, data)
}

// 菜谱评分列表
export function getRatings(id) {
  return request.get(`/recipe/${id}/ratings`)
}
