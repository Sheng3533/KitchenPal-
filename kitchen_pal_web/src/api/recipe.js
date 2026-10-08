import request from './request'

// 获取食谱列表（支持分类筛选和关键词搜索）
export function getRecipeList(params = {}) {
  return request.get('/recipe/list', { params })
}

// 获取食谱详情
export function getRecipeDetail(id) {
  return request.get(`/recipe/${id}`)
}

// 获取首页推荐食谱
export function getHeroRecipes() {
  return request.get('/recipe/hero')
}

// 获取所有分类
export function getCategories() {
  return request.get('/recipe/categories')
}

// 新增食谱
export function createRecipe(data) {
  return request.post('/recipe', data)
}

// 更新食谱
export function updateRecipe(id, data) {
  return request.put(`/recipe/${id}`, data)
}

// 删除食谱
export function deleteRecipe(id) {
  return request.delete(`/recipe/${id}`)
}

// 切换推荐状态
export function toggleRecipeHero(id) {
  return request.put(`/recipe/${id}/hero`)
}
