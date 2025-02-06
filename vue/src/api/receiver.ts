import request from '@/utils/request'
import type { searchReceiver } from '../views/Receiver/type'

// 获取列表
export async function getPageReceiver(data: searchReceiver): Promise<any> {
	return await request({
		url: '/receiver/getPageReceiver',
		method: 'post',
		data
	})
}

// 新增接收方
export async function saveReceiver(data: any): Promise<any> {
	return await request({
		url: '/receiver/saveReceiver',
		method: 'post',
		data
	})
}

// 更新接收方
export async function updateReceiver(data: any): Promise<any> {
	return await request({
		url: '/receiver/updateReceiver',
		method: 'post',
		data
	})
}

// 删除接收方
export async function deleteReceiver(data: any): Promise<any> {
	return await request({
		url: '/receiver/deleteReceiver',
		method: 'post',
		data
	})
}
