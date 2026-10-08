import request from './request'

export function chat(data) {
  return request.post('/assistant/chat', data)
}

export function generateRecipe(data) {
  return request.post('/assistant/generate-recipe', data)
}

export function analyzeIngredients(data) {
  return request.post('/assistant/analyze-ingredients', data)
}

export function cookingTips(data) {
  return request.post('/assistant/cooking-tips', data)
}

export function askKnowledge(data) {
  return request.post('/assistant/ask', data)
}
