import request from './request'

// 食材图片识别（multipart/form-data）
export function recognizeImage(imageFile) {
  const formData = new FormData()
  formData.append('image', imageFile)
  return request.post('/scan/recognize', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000,
  })
}

// 获取全部识别日志
export function getScanLogs() {
  return request.get('/scan/logs')
}

// 获取单条日志详情
export function getScanLogDetail(id) {
  return request.get(`/scan/logs/${id}`)
}

// 删除日志
export function deleteScanLog(id) {
  return request.delete(`/scan/logs/${id}`)
}
