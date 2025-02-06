import type { Dayjs } from 'dayjs'

export interface AppItem {
	appId: number
	appName: string
}

export interface MessageItem {
	code: string
	name: string
}

export interface TemplateItem {
	templateId: number
	templateStatus: number
}

export interface Pagination {
	/**
	 * 当前页面序号
	 */
	currentPage: number
	/**
	 * 页面大小
	 */
	pageSize: number
}
/**
 * 搜索框接口
 */
export interface SearchModel {
	/**
	 * 模板名
	 */
	templateName?: string
	/**
	 * 推送范围
	 */
	pushRange?: number
	/**
	 * 用户类型
	 */
	usersType?: number
	/**
	 * 日期
	 */
	period?: [Dayjs, Dayjs]
	/**
	 * 日期开始时间
	 */
	startTime?: string
	/**
	 * 日期结束时间
	 */
	endTime?: string
}

/**
 * 发送搜索框信息接口
 */
export interface SearchMessage extends SearchModel, Pagination {
	/**
	 * 模板状态
	 */
	templateStatus?: number | boolean
}

/**
 * 表单数据
 */
export interface MessageTemplate {
	/**
	 * 模板id
	 */
	templateId: number
	/**
	 * 模板名
	 */
	templateName: string
	/**
	 * 推送范围
	 */
	pushRange: number
	/**
	 * 用户类型
	 */
	usersType: number
	/**
	 * 推送方式
	 */
	pushWays?: string
	/**
	 * 渠道选择
	 */
	channelType: number | string
	/**
	 * 消息类型
	 */
	messageType: number
	/**
	 * 模板累计使用数
	 */
	useCount: number
	/**
	 * 模板状态
	 */
	templateStatus: number | boolean
	/**
	 * 创建用户
	 */
	createUser: string
	/**
	 * 创建时间
	 */
	createTime: string
	/**
	 * Appid
	 */
	appId: number
	/**
	 * App名称
	 */
	appName: string
}

/**
 * 新增接口
 */
export interface AddTemp {
	/**
	 * 模板名
	 */
	templateName: string
	/**
	 * 推送范围
	 */
	pushRange: number | undefined
	/**
	 * 用户类型
	 */
	usersType: number | undefined
	/**
	 * 用户类型
	 */
	pushWays: string
	/**
	 * 渠道选择
	 */
	channelType?: string | undefined
	/**
	 * 消息类型
	 */
	messageType?: string
	/**
	 * Appid
	 */
	appId: number | undefined
	/**
	 * 模板状态
	 */
	templateStatus: number | boolean
}

/**
 * 发送消息接口
 */
export interface SendTestMessage {
	/**
	 * 模板id
	 */
	templateId: number
	/**
	 * 用户列表
	 */
	users: string[]
	/**
	 * 传递参数
	 */
	paramMap: JSON
	/**
	 * 重试次数
	 */
	retry: number | undefined
}
