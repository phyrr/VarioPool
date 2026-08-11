import { defineStore } from 'pinia'
import { login as loginApi, getUser } from '../api/pool'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('variopool_token') || '',
    username: localStorage.getItem('variopool_username') || ''
  }),
  actions: {
    async login(form) {
      const data = await loginApi(form)
      this.token = data.accessToken
      this.username = data.username
      localStorage.setItem('variopool_token', data.accessToken)
      localStorage.setItem('variopool_username', data.username)
    },
    async fetchUser() {
      const data = await getUser()
      this.username = data.username
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('variopool_token')
      localStorage.removeItem('variopool_username')
    }
  }
})
