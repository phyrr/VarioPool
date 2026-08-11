<template>
  <div class="pools-layout">
    <div class="main-column">
      <div class="page-header">
        <div>
          <h1 class="page-title">My Thread Pools</h1>
          <div class="page-subtitle">支持 Nacos 配置读取、参数编辑、运行时监控</div>
        </div>
        <div class="header-actions">
          <el-select v-model="selectedApp" clearable placeholder="筛选应用" class="app-select" @change="loadData">
            <el-option v-for="app in apps" :key="app.name" :label="app.name" :value="app.name" />
          </el-select>
          <el-button type="primary" round :loading="loading" @click="loadData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <div class="tab-row">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-chip"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-loading="loading" class="pool-grid">
        <div v-if="displayPools.length === 0 && !loading" class="empty-card">
          暂无线程池数据，请确认 Nacos 配置和 example 应用已启动
        </div>

        <article
          v-for="(pool, index) in displayPools"
          :key="`${pool.appName}-${pool.poolId}`"
          class="pool-card"
          :style="{ background: cardThemes[index % cardThemes.length].bg }"
        >
          <div class="pool-card-top">
            <div class="pool-icon" :style="{ background: cardThemes[index % cardThemes.length].accent }">
              <el-icon><Cpu /></el-icon>
            </div>
            <el-tag round effect="light">{{ pool.configSource || 'Nacos' }}</el-tag>
          </div>

          <h3 class="pool-name">{{ pool.poolId }}</h3>
          <div class="pool-meta">{{ pool.appName }}</div>

          <div class="pool-stats">
            <div>
              <span>核心 / 最大</span>
              <strong>{{ pool.corePoolSize }} / {{ pool.maximumPoolSize }}</strong>
            </div>
            <div>
              <span>队列容量</span>
              <strong>{{ pool.queueCapacity }}</strong>
            </div>
            <div>
              <span>活跃线程</span>
              <strong>{{ pool.activeCount ?? 0 }}</strong>
            </div>
            <div>
              <span>拒绝次数</span>
              <strong>{{ pool.rejectCount ?? 0 }}</strong>
            </div>
          </div>

          <div class="pool-actions">
            <button class="action-btn ghost" @click="openEdit(pool)">编辑参数</button>
            <button class="action-btn primary" @click="goMonitor(pool)">进入监控</button>
          </div>
        </article>
      </div>
    </div>

    <aside class="stats-column">
      <div class="stats-card highlight">
        <div class="stats-card-label">Overview</div>
        <div class="stats-card-value">{{ pools.length }}</div>
        <div class="stats-card-desc">当前管理的线程池数量</div>
        <div class="stats-progress">
          <div class="stats-progress-bar" :style="{ width: `${Math.min(overviewRate, 100)}%` }" />
        </div>
        <div class="stats-progress-text">活跃率概览 {{ overviewRate.toFixed(0) }}%</div>
      </div>

      <div class="stats-card">
        <div class="stats-card-title">Runtime Stats</div>
        <div v-for="item in sideStats" :key="item.label" class="side-stat-item">
          <div class="side-stat-icon" :style="{ background: item.bg, color: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="side-stat-content">
            <div class="side-stat-label">{{ item.label }}</div>
            <div class="side-stat-value">{{ item.value }}</div>
          </div>
          <el-icon class="side-stat-arrow"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="stats-card featured">
        <div class="featured-badge">Live Monitor</div>
        <div class="featured-title">实时拉取 example 实例指标，每 5 秒自动刷新。</div>
        <button class="featured-btn" @click="openFirstMonitor">打开监控</button>
      </div>
    </aside>

    <PoolEditDialog v-model:visible="editVisible" :pool="currentPool" @saved="loadData" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listApps, listThreadPools } from '../api/pool'
import PoolEditDialog from '../components/PoolEditDialog.vue'

const router = useRouter()
const loading = ref(false)
const apps = ref([])
const pools = ref([])
const selectedApp = ref('')
const editVisible = ref(false)
const currentPool = ref(null)
const activeTab = ref('all')

const tabs = [
  { key: 'all', label: 'All Pools' },
  { key: 'active', label: 'Active' },
  { key: 'nacos', label: 'Nacos' }
]

const cardThemes = [
  { bg: 'linear-gradient(180deg, #eef4ff 0%, #ffffff 100%)', accent: '#dbeafe' },
  { bg: 'linear-gradient(180deg, #f3ebff 0%, #ffffff 100%)', accent: '#e9d5ff' },
  { bg: 'linear-gradient(180deg, #fff4ec 0%, #ffffff 100%)', accent: '#fed7aa' }
]

const displayPools = computed(() => {
  let list = pools.value
  if (activeTab.value === 'active') {
    list = list.filter((item) => (item.activeCount || 0) > 0)
  }
  if (activeTab.value === 'nacos') {
    list = list.filter((item) => item.configSource === 'Nacos')
  }
  return list
})

const overviewRate = computed(() => {
  if (pools.value.length === 0) return 0
  const totalMax = pools.value.reduce((sum, item) => sum + (item.maximumPoolSize || 0), 0)
  const totalActive = pools.value.reduce((sum, item) => sum + (item.activeCount || 0), 0)
  return totalMax === 0 ? 0 : (totalActive / totalMax) * 100
})

const sideStats = computed(() => [
  {
    label: 'Active Threads',
    value: pools.value.reduce((sum, item) => sum + (item.activeCount || 0), 0),
    icon: 'Odometer',
    bg: '#eef2ff',
    color: '#4f46e5'
  },
  {
    label: 'Queue Tasks',
    value: pools.value.reduce((sum, item) => sum + (item.queueSize || 0), 0),
    icon: 'List',
    bg: '#fff4ec',
    color: '#ea580c'
  },
  {
    label: 'Reject Count',
    value: pools.value.reduce((sum, item) => sum + (item.rejectCount || 0), 0),
    icon: 'Warning',
    bg: '#f3ebff',
    color: '#9333ea'
  }
])

async function loadApps() {
  apps.value = await listApps()
}

async function loadData() {
  loading.value = true
  try {
    pools.value = await listThreadPools(selectedApp.value || undefined)
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  currentPool.value = { ...row }
  editVisible.value = true
}

function goMonitor(row) {
  router.push({
    name: 'monitor',
    params: { poolId: row.poolId },
    query: { appName: row.appName }
  })
}

function openFirstMonitor() {
  if (pools.value.length === 0) return
  goMonitor(pools.value[0])
}

onMounted(async () => {
  await loadApps()
  await loadData()
})
</script>

<style scoped>
.pools-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 22px;
}

.main-column {
  min-width: 0;
}

.header-actions,
.pool-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.app-select {
  width: 220px;
}

.tab-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-bottom: 24px;
}

.tab-chip {
  border: none;
  background: transparent;
  color: #94a3b8;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  padding: 0 0 10px;
  border-bottom: 3px solid transparent;
}

.tab-chip.active {
  color: #0f172a;
  border-bottom-color: #4f46e5;
}

.pool-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 18px;
}

.empty-card {
  grid-column: 1 / -1;
  padding: 48px;
  text-align: center;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
  color: #94a3b8;
  box-shadow: var(--vp-shadow-soft);
}

.pool-card {
  border-radius: 28px;
  padding: 24px;
  box-shadow: var(--vp-shadow-soft);
  border: 1px solid rgba(255, 255, 255, 0.85);
}

.pool-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pool-icon {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  color: #4338ca;
  font-size: 22px;
}

.pool-name {
  margin: 18px 0 6px;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.pool-meta {
  color: #64748b;
  font-size: 14px;
}

.pool-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 22px;
}

.pool-stats span {
  display: block;
  color: #94a3b8;
  font-size: 12px;
  margin-bottom: 4px;
}

.pool-stats strong {
  font-size: 18px;
  font-weight: 800;
}

.pool-actions {
  margin-top: 24px;
}

.action-btn {
  flex: 1;
  border: none;
  border-radius: 999px;
  padding: 12px 16px;
  font-weight: 700;
  cursor: pointer;
}

.action-btn.ghost {
  background: rgba(255, 255, 255, 0.82);
  color: #475569;
}

.action-btn.primary {
  background: #111827;
  color: #fff;
}

.stats-column {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.stats-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 28px;
  padding: 22px;
  box-shadow: var(--vp-shadow-soft);
  border: 1px solid rgba(255, 255, 255, 0.85);
}

.stats-card.highlight {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.stats-card-label,
.featured-badge {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.stats-card-value {
  margin-top: 10px;
  font-size: 44px;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.stats-card-desc,
.featured-title {
  margin-top: 8px;
  color: #64748b;
  line-height: 1.6;
}

.stats-progress {
  margin-top: 18px;
  height: 10px;
  border-radius: 999px;
  background: #e2e8f0;
  overflow: hidden;
}

.stats-progress-bar {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

.stats-progress-text {
  margin-top: 10px;
  font-size: 13px;
  color: #64748b;
}

.stats-card-title {
  font-size: 18px;
  font-weight: 800;
  margin-bottom: 14px;
}

.side-stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.side-stat-item + .side-stat-item {
  border-top: 1px solid #eef2f7;
}

.side-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
}

.side-stat-label {
  color: #64748b;
  font-size: 13px;
}

.side-stat-value {
  font-size: 22px;
  font-weight: 800;
}

.side-stat-arrow {
  margin-left: auto;
  color: #cbd5e1;
}

.stats-card.featured {
  background: linear-gradient(180deg, #111827 0%, #312e81 100%);
  color: #fff;
}

.featured-badge {
  color: rgba(255, 255, 255, 0.72);
}

.featured-title {
  color: rgba(255, 255, 255, 0.88);
  font-size: 18px;
  font-weight: 700;
}

.featured-btn {
  margin-top: 18px;
  width: 100%;
  border: none;
  border-radius: 999px;
  padding: 14px 18px;
  background: #fff;
  color: #111827;
  font-weight: 800;
  cursor: pointer;
}

@media (max-width: 1200px) {
  .pools-layout {
    grid-template-columns: 1fr;
  }

  .stats-column {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  }
}
</style>
