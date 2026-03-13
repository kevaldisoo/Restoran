import { defineStore } from 'pinia'
import { api } from '@/api/index.js'

export const useBookingStore = defineStore('broneeringud', {
  state: () => ({
    broneeringud: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchBroneeringud() {
      this.loading = true
      this.error = null
      try {
        this.broneeringud = await api.getBroneeringud()
      } catch (e) {
        this.error = e.message
      } finally {
        this.loading = false
      }
    },

    async createBroneering(data) {
      this.loading = true
      this.error = null
      try {
        const created = await api.createBroneering(data)
        this.broneeringud.push(created)
        return created
      } catch (e) {
        this.error = e.message
        throw e
      } finally {
        this.loading = false
      }
    },
  },
})
