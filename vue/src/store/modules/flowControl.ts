import { defineStore } from 'pinia'
import { getFlowControlData } from '@/api/flowControl'
import type { FlowControlData } from '@/views/FlowControl/type'

export const useFlowControlStore = defineStore('flowControl', {
  state: () => ({
    flowControlData: null as FlowControlData | null
  }),
  
  actions: {
    async getFlowControlData() {
      try {
        const res = await getFlowControlData()
        this.flowControlData = res.data
        return res.data
      } catch (error) {
        console.error('Error fetching flow control data:', error)
        throw error
      }
    }
  }
}) 