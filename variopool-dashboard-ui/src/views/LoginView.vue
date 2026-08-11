<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-showcase">
        <div class="showcase-badge">VarioPool Dashboard</div>
        <h1>Manage dynamic thread pools with a clean, modern control panel.</h1>
        <p>实时读取 Nacos 配置，编辑参数，查看运行时指标与监控图表。</p>
        <div class="showcase-cards">
          <div class="showcase-card blue">Nacos Config</div>
          <div class="showcase-card purple">Live Metrics</div>
          <div class="showcase-card orange">Hot Refresh</div>
        </div>
      </section>

      <section class="login-card">
        <h2>Welcome back</h2>
        <p>登录 VarioPool 控制台</p>
        <el-form :model="form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
          </el-form-item>
          <el-button type="primary" size="large" round style="width: 100%" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form>
        <div class="hint">默认账号：admin / admin</div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin'
})

async function handleLogin() {
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    router.push('/pools')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(99, 102, 241, 0.12), transparent 30%),
    radial-gradient(circle at bottom right, rgba(255, 138, 76, 0.12), transparent 28%),
    var(--vp-bg);
}

.login-shell {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 420px;
  gap: 28px;
  align-items: center;
}

.login-showcase h1 {
  margin: 18px 0 16px;
  font-size: 42px;
  line-height: 1.15;
  letter-spacing: -0.04em;
}

.login-showcase p {
  color: #64748b;
  font-size: 16px;
  line-height: 1.7;
}

.showcase-badge {
  display: inline-block;
  padding: 8px 14px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4338ca;
  font-weight: 700;
}

.showcase-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.showcase-card {
  padding: 14px 18px;
  border-radius: 18px;
  font-weight: 700;
  box-shadow: var(--vp-shadow-soft);
}

.showcase-card.blue { background: #eaf3ff; color: #2563eb; }
.showcase-card.purple { background: #f3ebff; color: #9333ea; }
.showcase-card.orange { background: #fff4ec; color: #ea580c; }

.login-card {
  padding: 36px;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--vp-shadow);
  border: 1px solid rgba(255, 255, 255, 0.85);
}

.login-card h2 {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
}

.login-card p {
  color: #64748b;
}

.hint {
  margin-top: 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 900px) {
  .login-shell {
    grid-template-columns: 1fr;
  }
}
</style>
