<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-logo">VP</div>
        <div>
          <div class="brand-title">VarioPool</div>
          <div class="brand-subtitle">动态线程池控制台</div>
        </div>
      </div>

      <div class="profile-card">
        <div class="avatar">{{ avatarText }}</div>
        <div>
          <div class="profile-name">{{ auth.username }}</div>
          <div class="profile-role">Platform Admin</div>
        </div>
      </div>

      <nav class="nav-list">
        <router-link to="/pools" class="nav-item" :class="{ active: isPoolsRoute }">
          <el-icon><Grid /></el-icon>
          <span>线程池列表</span>
        </router-link>
        <router-link
          to="/monitor/order-pool?appName=variopool-example"
          class="nav-item"
          :class="{ active: route.name === 'monitor' }"
        >
          <el-icon><DataLine /></el-icon>
          <span>实时监控</span>
        </router-link>
      </nav>

      <div class="sidebar-card">
        <div class="sidebar-card-badge">Tips</div>
        <div class="sidebar-card-title">配置变更会发布到 Nacos</div>
        <div class="sidebar-card-desc">也可选择同步到 Redis，实现双通道热更新。</div>
      </div>
    </aside>

    <main class="main-area">
      <header class="topbar">
        <div class="search-box">
          <el-icon><Search /></el-icon>
          <span>Search thread pools, apps, metrics...</span>
        </div>
        <div class="topbar-actions">
          <button class="icon-btn"><el-icon><Bell /></el-icon></button>
          <button class="logout-btn" @click="logout">退出</button>
        </div>
      </header>

      <section class="content-area">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const avatarText = computed(() => (auth.username || 'A').slice(0, 1).toUpperCase())
const isPoolsRoute = computed(() => route.path.startsWith('/pools') || route.name === 'monitor')

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 18px;
  padding: 18px;
  background:
    radial-gradient(circle at top left, rgba(99, 102, 241, 0.08), transparent 28%),
    radial-gradient(circle at top right, rgba(255, 138, 76, 0.08), transparent 24%),
    var(--vp-bg);
}

.sidebar {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 32px;
  padding: 24px 18px;
  box-shadow: var(--vp-shadow);
  border: 1px solid rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 36px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 10px 22px;
}

.brand-logo {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 10px 24px rgba(99, 102, 241, 0.28);
}

.brand-title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: var(--vp-text-muted);
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 20px;
  background: #f8fafc;
  margin-bottom: 18px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #ff8a4c, #fb7185);
  color: #fff;
  font-weight: 700;
}

.profile-name {
  font-weight: 700;
}

.profile-role {
  margin-top: 2px;
  font-size: 12px;
  color: var(--vp-text-muted);
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  color: #64748b;
  font-weight: 600;
  transition: 0.2s ease;
}

.nav-item:hover,
.nav-item.active {
  background: #eef2ff;
  color: #4338ca;
}

.sidebar-card {
  margin-top: auto;
  padding: 20px;
  border-radius: 24px;
  background: linear-gradient(180deg, #f5f3ff 0%, #fff7ed 100%);
}

.sidebar-card-badge {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.75);
  font-size: 12px;
  font-weight: 700;
  color: #7c3aed;
}

.sidebar-card-title {
  margin-top: 14px;
  font-size: 18px;
  font-weight: 800;
  line-height: 1.4;
}

.sidebar-card-desc {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.main-area {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 4px;
}

.search-box {
  flex: 1;
  max-width: 520px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  color: #94a3b8;
  box-shadow: var(--vp-shadow-soft);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn,
.logout-btn {
  border: none;
  cursor: pointer;
  font-weight: 600;
}

.icon-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #fff;
  box-shadow: var(--vp-shadow-soft);
  color: #64748b;
}

.logout-btn {
  padding: 14px 22px;
  border-radius: 999px;
  background: #fff;
  color: #475569;
  box-shadow: var(--vp-shadow-soft);
}

.content-area {
  min-height: 0;
}
</style>
