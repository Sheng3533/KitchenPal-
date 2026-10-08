import request from './request'

// 获取管理端 Dashboard 聚合统计（KPI + 7天趋势 + 分类占比 + 热门TOP10）
export function getStatsOverview() {
  return request.get('/stats/overview')
}
