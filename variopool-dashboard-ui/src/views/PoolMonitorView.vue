<template>
  <div class="monitor-layout">
    <div class="page-header">
      <div>
        <h1 class="page-title">Monitor - {{ poolId }}</h1>
        <div class="page-subtitle">实时拉取应用实例运行指标，每 5 秒自动刷新</div>
      </div>
      <div class="header-actions">
        <button class="vp-btn-ghost" @click="router.push('/pools')">返回列表</button>
        <el-button type="primary" round :loading="loading" @click="loadMetrics">刷新</el-button>
      </div>
    </div>

    <div v-if="metrics.length === 0 && !loading" class="empty-card">
      暂无运行数据，请确认示例应用已启动
    </div>

    <div v-for="item in metrics" :key="item.agentUrl" class="monitor-panel">
      <div class="monitor-top">
        <div>
          <div class="agent-badge">Instance</div>
          <div class="agent-url">{{ item.agentUrl }}</div>
          <div class="agent-sub">Pool · {{ item.poolId }}</div>
        </div>
        <div class="rate-pill" :class="{ danger: item.activeRate > 80 }">
          活跃率 {{ item.activeRate?.toFixed(1) }}%
        </div>
      </div>

      <div class="metric-grid">
        <div v-for="card in metricCards(item)" :key="card.label" class="metric-card" :style="{ background: card.bg }">
          <div class="metric-label">{{ card.label }}</div>
          <div class="metric-value">{{ card.value }}</div>
        </div>
      </div>

      <div class="charts">
        <div class="chart-card">
          <v-chart class="chart" :option="buildActiveChart(item)" autoresize />
        </div>
        <div class="chart-card">
          <v-chart class="chart" :option="buildQueueChart(item)" autoresize />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GaugeChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, TitleComponent, LegendComponent } from 'echarts/components'
import { listMetrics } from '../api/pool'

use([CanvasRenderer, GaugeChart, PieChart, GridComponent, TooltipComponent, TitleComponent, LegendComponent])

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const metrics = ref([])
const poolId = computed(() => route.params.poolId)
const appName = computed(() => route.query.appName)
let timer = null

function metricCards(item) {
  return [
    { label: '活跃线程', value: `${item.activeCount} / ${item.maximumPoolSize}`, bg: 'linear-gradient(180deg, #eef4ff, #fff)' },
    { label: '当前线程数', value: item.poolSize, bg: 'linear-gradient(180deg, #f3ebff, #fff)' },
    { label: '队列任务', value: item.queueSize, bg: 'linear-gradient(180deg, #fff4ec, #fff)' },
    { label: '完成任务', value: item.completedTaskCount, bg: 'linear-gradient(180deg, #ecfdf5, #fff)' },
    { label: '拒绝次数', value: item.rejectCount, bg: 'linear-gradient(180deg, #fef2f2, #fff)' }
  ]
}

function gaugeColor(rate) {
  if (rate >= 85) return '#ef4444'
  if (rate >= 60) return '#f59e0b'
  return '#4f46e5'
}

function buildActiveChart(item) {
  const value = Number(item.activeRate?.toFixed(1) || 0)
  const color = gaugeColor(value)
  return {
    title: {
      text: '线程活跃率',
      left: 'center',
      top: 10,
      textStyle: { fontSize: 16, fontWeight: 700, color: '#334155' }
    },
    series: [{
      type: 'gauge',
      startAngle: 210,
      endAngle: -30,
      min: 0,
      max: 100,
      radius: '88%',
      center: ['50%', '60%'],
      pointer: {
        show: true,
        length: '58%',
        width: 5,
        itemStyle: { color }
      },
      progress: {
        show: true,
        overlap: false,
        roundCap: true,
        clip: false,
        width: 16,
        itemStyle: { color }
      },
      axisLine: {
        roundCap: true,
        lineStyle: {
          width: 16,
          color: [[0.6, '#dbeafe'], [0.85, '#fde68a'], [1, '#fecaca']]
        }
      },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: {
        distance: 22,
        color: '#94a3b8',
        fontSize: 11,
        formatter: (val) => (val % 20 === 0 ? `${val}%` : '')
      },
      detail: {
        valueAnimation: true,
        formatter: '{value}%',
        fontSize: 30,
        fontWeight: 700,
        color: '#0f172a',
        offsetCenter: [0, '28%']
      },
      title: { show: false },
      data: [{ value }]
    }]
  }
}

function buildQueueChart(item) {
  const used = item.queueSize || 0
  const remaining = item.queueRemainingCapacity || 0
  const total = used + remaining
  const usageRate = total === 0 ? 0 : Number(((used / total) * 100).toFixed(1))
  return {
    title: {
      text: '队列使用情况',
      left: 'center',
      top: 10,
      textStyle: { fontSize: 16, fontWeight: 700, color: '#334155' }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 10,
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#64748b' }
    },
    graphic: [{
      type: 'text',
      left: 'center',
      top: '52%',
      style: {
        text: `${usageRate}%`,
        fill: '#0f172a',
        fontSize: 28,
        fontWeight: 700,
        textAlign: 'center'
      }
    }, {
      type: 'text',
      left: 'center',
      top: '62%',
      style: {
        text: '使用率',
        fill: '#94a3b8',
        fontSize: 12,
        textAlign: 'center'
      }
    }],
    series: [{
      type: 'pie',
      radius: ['52%', '74%'],
      center: ['50%', '58%'],
      avoidLabelOverlap: true,
      label: { show: false },
      labelLine: { show: false },
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      data: [
        { value: used, name: '已使用', itemStyle: { color: '#4f46e5' } },
        { value: remaining, name: '剩余容量', itemStyle: { color: '#e2e8f0' } }
      ]
    }]
  }
}

async function loadMetrics() {
  loading.value = true
  try {
    metrics.value = await listMetrics(appName.value, poolId.value)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadMetrics()
  timer = setInterval(loadMetrics, 5000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.monitor-layout {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.empty-card,
.monitor-panel {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 28px;
  padding: 24px;
  box-shadow: var(--vp-shadow-soft);
  border: 1px solid rgba(255, 255, 255, 0.85);
}

.empty-card {
  text-align: center;
  color: #94a3b8;
  padding: 48px;
}

.monitor-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.agent-badge {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4338ca;
  font-size: 12px;
  font-weight: 700;
}

.agent-url {
  margin-top: 12px;
  font-size: 22px;
  font-weight: 800;
}

.agent-sub {
  margin-top: 6px;
  color: #64748b;
}

.rate-pill {
  padding: 10px 16px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #15803d;
  font-weight: 700;
}

.rate-pill.danger {
  background: #fef2f2;
  color: #dc2626;
}

.charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.chart-card {
  background: #f8fafc;
  border-radius: 24px;
  padding: 12px;
  border: 1px solid var(--vp-border);
}

.chart {
  height: 300px;
}
</style>
