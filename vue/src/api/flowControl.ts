import request from '@/utils/request'
import type { R } from '@/types'
import { FlowControlConfig } from '@/views/FlowControl/type'
import { FlowControlData } from '../views/FlowControl/type'


export async function getFlowControlConfigByCode(code: string): Promise<R<FlowControlConfig>> {
  return await request({
    url: '/flowControl/getFlowControlConfigByCode',
    method: 'post',
    data: { code }
  })
} 

export async function updateFlowControlConfig(code: string, config: FlowControlConfig): Promise<R<void>> {
  config.code = code;
  return await request({
    url: '/flowControl/updateFlowControlConfig',
    method: 'post',
    data: config
  })
}

export async function getFlowControlData(code: string): Promise<R<FlowControlData>> {
  return await request({
    url: '/flowControl/getMetrics/'+code,
    method: 'post'
  })
}

