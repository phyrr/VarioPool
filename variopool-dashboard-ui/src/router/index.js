import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/pools',
    children: [
      {
        path: 'pools',
        name: 'pools',
        component: () => import('../views/PoolListView.vue')
      },
      {
        path: 'monitor/:poolId',
        name: 'monitor',
        component: () => import('../views/PoolMonitorView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.token) {
    return '/login'
  }
  if (to.path === '/login' && auth.token) {
    return '/pools'
  }
})

export default router
