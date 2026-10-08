<template>
  <div>
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div v-for="stat in kpiData" :key="stat.label"
           class="flex items-center gap-3 p-4 card min-w-[120px]">
        <div class="w-10 h-10 rounded-lg flex items-center justify-center shrink-0"
             style="background: var(--brand-primary-lightest);">
          <component :is="stat.icon" class="w-5 h-5" style="color: var(--brand-primary);" />
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-2xl font-bold whitespace-nowrap text-foreground">{{ stat.value }}</div>
          <div class="text-sm truncate text-muted-foreground">{{ stat.label }}</div>
        </div>
        <div class="flex items-center gap-1 text-xs whitespace-nowrap shrink-0"
             style="color: var(--state-success);">
          <TrendingUp class="w-3.5 h-3.5" />
          <span>+5.2%</span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-5 gap-4 mb-6">
      <div class="lg:col-span-3 p-5 card">
        <h3 class="text-sm font-semibold mb-4 truncate text-foreground">近7天烹饪趋势</h3>
        <div style="position:relative;height:280px;">
          <canvas ref="lineChartRef"></canvas>
        </div>
      </div>
      <div class="lg:col-span-2 p-5 card">
        <h3 class="text-sm font-semibold mb-4 truncate text-foreground">食谱分类占比</h3>
        <div style="position:relative;height:280px;">
          <canvas ref="doughnutChartRef"></canvas>
        </div>
      </div>
    </div>

    <div class="p-5 card">
      <h3 class="text-sm font-semibold mb-4 truncate text-foreground">热门食谱TOP10</h3>
      <div style="position:relative;height:360px;">
        <canvas ref="barChartRef"></canvas>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import { Users, BookOpen, ChefHat, Bell, TrendingUp } from 'lucide-vue-next'
import { getStatsOverview } from '@/api/stats'

Chart.register(...registerables)

const lineChartRef = ref(null)
const doughnutChartRef = ref(null)
const barChartRef = ref(null)

let lineChart = null
let doughnutChart = null
let barChart = null

// 后端聚合统计（/api/stats/overview）
const stats = ref(null)
// API是否加载失败，失败时使用Mock数据兜底
const apiFailed = ref(false)

const fmtNum = (n) => (n == null ? '--' : Number(n).toLocaleString('zh-CN'))

// 原始Mock数据，作为后端API失败时的兜底
const mockKpi = { totalUsers: 12856, totalRecipes: 348, todayCooking: 256, pendingNotifications: 18 }
const mockDoughnutData = {
  labels: ['中餐', '西餐', '甜点', '日料', '烘焙', '其他'],
  data: [35, 22, 18, 12, 8, 5],
}
const mockBarData = {
  labels: ['红烧肉', '番茄炒蛋', '麻婆豆腐', '宫保鸡丁', '糖醋排骨', '鱼香肉丝', '回锅肉', '青椒肉丝', '酸菜鱼', '水煮牛肉'],
  data: [892, 756, 698, 645, 612, 589, 534, 498, 467, 423],
}
const mockLineData = {
  labels: ['7/11', '7/12', '7/13', '7/14', '7/15', '7/16', '7/17'],
  data: [152, 189, 143, 201, 178, 224, 196],
}

// KPI卡片：全部来自后端聚合统计
const kpiData = computed(() => {
  const s = stats.value
  return [
    { label: '用户总数', value: fmtNum(s ? s.totalUsers : mockKpi.totalUsers), icon: Users },
    { label: '食谱总数', value: fmtNum(s ? s.totalRecipes : mockKpi.totalRecipes), icon: BookOpen },
    { label: '今日烹饪', value: fmtNum(s ? s.todayCooking : mockKpi.todayCooking), icon: ChefHat },
    { label: '待处理通知', value: fmtNum(s ? s.pendingNotifications : mockKpi.pendingNotifications), icon: Bell },
  ]
})

// 近7天烹饪趋势（折线图）
const lineChartData = computed(() => {
  if (apiFailed.value || !stats.value?.weeklyTrend) return mockLineData
  return {
    labels: stats.value.weeklyTrend.map(d => d.label),
    data: stats.value.weeklyTrend.map(d => d.count),
  }
})

// 食谱分类占比（环形图）
const doughnutChartData = computed(() => {
  if (apiFailed.value || !stats.value?.categoryDist) return mockDoughnutData
  return {
    labels: Object.keys(stats.value.categoryDist),
    data: Object.values(stats.value.categoryDist),
  }
})

// 热门食谱TOP10（柱状图，按评分）
const barChartData = computed(() => {
  if (apiFailed.value || !stats.value?.topRecipes) return mockBarData
  return {
    labels: stats.value.topRecipes.map(r => r.title),
    data: stats.value.topRecipes.map(r => r.rating),
  }
})

const fontFamily = "'Inter', 'Noto Sans SC', sans-serif"
const doughnutColors = ['#F97316', '#FB923C', '#FED7AA', '#EA580C', '#16A34A', '#2563EB', '#9333EA', '#0891B2', '#DC2626', '#65A30D']

onMounted(async () => {
  await loadStats()
  await nextTick()
  initCharts()
})

async function loadStats() {
  try {
    const data = await getStatsOverview()
    if (data && typeof data === 'object') {
      stats.value = data
      apiFailed.value = false
    } else {
      apiFailed.value = true
    }
  } catch (e) {
    console.error('[Dashboard] 获取统计数据失败，使用Mock数据兜底', e)
    apiFailed.value = true
  }
}

onBeforeUnmount(() => {
  lineChart?.destroy()
  doughnutChart?.destroy()
  barChart?.destroy()
})

function initCharts() {
  if (lineChartRef.value) {
    const { labels, data } = lineChartData.value
    lineChart = new Chart(lineChartRef.value, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: '烹饪次数',
          data,
          borderColor: '#F97316',
          backgroundColor: 'rgba(249, 115, 22, 0.08)',
          borderWidth: 2,
          fill: true,
          tension: 0.4,
          pointBackgroundColor: '#F97316',
          pointBorderColor: '#FFFFFF',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#171717',
            titleFont: { family: fontFamily },
            bodyFont: { family: fontFamily },
            padding: 10,
            cornerRadius: 8
          }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { color: '#737373', font: { size: 12 } },
            border: { display: false }
          },
          y: {
            grid: { color: '#E5E5E5' },
            ticks: { color: '#737373', font: { size: 12 } },
            border: { display: false },
            beginAtZero: true
          }
        }
      }
    })
  }

  if (doughnutChartRef.value) {
    const { labels, data } = doughnutChartData.value
    doughnutChart = new Chart(doughnutChartRef.value, {
      type: 'doughnut',
      data: {
        labels: labels.length ? labels : ['暂无数据'],
        datasets: [{
          data: data.length ? data : [1],
          backgroundColor: doughnutColors,
          borderWidth: 2,
          borderColor: '#FFFFFF'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 16,
              usePointStyle: true,
              pointStyle: 'circle',
              font: { size: 12, family: fontFamily },
              color: '#525252'
            }
          },
          tooltip: {
            backgroundColor: '#171717',
            padding: 10,
            cornerRadius: 8
          }
        }
      }
    })
  }

  if (barChartRef.value) {
    const { labels, data } = barChartData.value
    barChart = new Chart(barChartRef.value, {
      type: 'bar',
      data: {
        labels: labels.length ? labels : ['暂无数据'],
        datasets: [{
          label: '评分',
          data: data.length ? data : [0],
          backgroundColor: '#F97316',
          borderRadius: 4,
          barThickness: 24
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: 'y',
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#171717',
            padding: 10,
            cornerRadius: 8
          }
        },
        scales: {
          x: {
            grid: { color: '#E5E5E5' },
            ticks: { color: '#737373', font: { size: 12 } },
            border: { display: false },
            beginAtZero: true
          },
          y: {
            grid: { display: false },
            ticks: { color: '#525252', font: { size: 12, family: fontFamily } },
            border: { display: false }
          }
        }
      }
    })
  }
}
</script>
