import axios from 'axios'
import { useStore } from '@/store';
import CryptoJS from 'crypto-js';
const service = axios.create({
	baseURL: '/backend',
	timeout: 20000
})

service.interceptors.request.use(
	(config) => {
		const store = useStore()
		const username = store.username
		const userId = store.userId
		if (username && userId) {
		  // 对 username 进行 MD5 加密
		  const uniqueId = username + "," + userId;
		  const md5Username = CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(uniqueId))
		  // 将加密后的 username 添加到请求头
		  config.headers['Authorization'] = md5Username
		}
		return config
	},
	async (err) => {
		return await Promise.reject(err)
	}
)

service.interceptors.response.use(
	async (res) => {
		if (res?.data.code === 200) return res.data
		return await Promise.reject(res?.data.errorMessage)
	},
	async (err) => {
		return await Promise.reject(err)
	}
)
export default service
