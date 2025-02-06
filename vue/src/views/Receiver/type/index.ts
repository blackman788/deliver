import type { Dayjs } from 'dayjs'

// 平台文件表格
export interface receiver {
	id?: string
	sender?: string
	password?: string
	encryption?: string
	secret?: string
	createTime?: string
}
// 搜索框接口
export interface searchReceiver {
	/**
	 * 发送方
	 */
	sender?: string

	/**
	 * 密码
	 */
	password?: string

	/**
	 * 加密方式
	 */
	encryption?: string

	/**
	 * 密钥
	 */
	secret?: string

	/**
	 * 当前页面序号
	 */
	currentPage: number

	/**
	 * 页面大小
	 */
	pageSize: number

	/**
	 * 起始日期
	 */
	startTime?: string


	/**
	 * 时间段
	 */
	perid?: [Dayjs, Dayjs]
}
