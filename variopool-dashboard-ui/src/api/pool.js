import http from './http'

export function login(data) {
  return http.post('/auth/login', data)
}

export function getUser() {
  return http.get('/auth/user')
}

export function listApps() {
  return http.get('/apps')
}

export function listThreadPools(appName) {
  return http.get('/thread-pools', { params: { appName } })
}

export function updateThreadPool(data) {
  return http.put('/thread-pool', data)
}

export function listMetrics(appName, poolId) {
  return http.get('/thread-pools/metrics', { params: { appName, poolId } })
}
