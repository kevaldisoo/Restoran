import { defineStore } from 'pinia'
import { api } from '@/api/index.js'

export const useTableStore = defineStore('lauad', {
  state: () => ({
    lauad: [],
    soovitused: [],
    loading: false,
    error: null,
    searched: false,
  }),

  getters: {
    // Build a quick lookup map: tableId → recommendation DTO
    recommendationMap(state) {
      const map = {}
      for (const r of state.soovitused) map[r.id] = r
      return map
    },
  },

  actions: {
    async fetchLauad() {
      this.loading = true
      this.error = null
      try {
        this.lauad = await api.getLauad()
      } catch (e) {
        this.error = e.message
      } finally {
        this.loading = false
      }
    },

    async fetchSoovitused(filter) {
      this.loading = true
      this.error = null
      try {
        this.soovitused = await api.getSoovitused(filter)
        this.searched = true
      } catch (e) {
        this.error = e.message
      } finally {
        this.loading = false
      }
    },

    clearSoovitused() {
      this.soovitused = []
      this.searched = false
    },
  },
})
