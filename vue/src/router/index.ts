import { createRouter, type RouteRecordRaw, createWebHistory } from 'vue-router'
import { useStore } from '../store'

const routes: RouteRecordRaw[] = [
	{
		path: '/',
		name: '首页',
		component: async () => await import('@/layouts/index.vue'),
		redirect: 'welcome',
		meta: {
			requiresAuth: true
		},
		children: [
			{
				path: 'welcome',
				name: '欢迎',
				component: async () => await import('@/views/Welcome/index.vue'),
				meta: {
					parent: 'welcome',
					title: '欢迎 - Deliver 企业消息推送平台'
				}
			},
			{
				path: 'dashboard',
				name: '控制面板',
				component: async () => await import('@/views/Dashboard/index.vue'),
				meta: {
					parent: '首页',
					title: '控制面板 - Deliver 企业消息推送平台'
				}
			},
			{
				path: 'template',
				name: '模板配置',
				component: async () => await import('@/views/Template/index.vue'),
				meta: {
					parent: '首页',
					title: '模板配置 - Deliver 企业消息推送平台'
				}
			},
			{
				path: 'app',
				name: '应用配置',
				component: async () => await import('@/views/App/index.vue'),
				meta: {
					parent: '首页',
					title: '应用配置 - Deliver 企业消息推送平台'
				}
			},
			{
				path: 'file',
				name: '文件管理',
				component: async () => await import('@/views/PlatformFile/index.vue'),
				meta: {
					parent: '首页',
					title: '平台文件管理 - Deliver 企业消息推送平台'
				}
			},
			{
				path: 'flowControl',
				name: '流控数据看板',
				component: async () => await import('@/views/FlowControl/index.vue'),
				meta: {
					parent: '首页',
					title: '流控数据看板 - Deliver 企业消息推送平台'
				}
			},
			{
				path: 'receiver',
				name: '接口调用方设置',
				component: async () => await import('@/views/Receiver/index.vue'),
				meta: {
					parent: '首页',
					title: '接口调用方设置 - Deliver 企业消息推送平台'
				}
			}
		]
	},
	{
		path: '/:pathMatch(.*)*',
		name: '404',
		component: async () => await import('@/views/404.vue'),
		meta: {
			title: 'Deliver 企业消息推送平台'
		}
	}
]

const router = createRouter({
	history: createWebHistory(),
	routes
})

const SSO_SERVER = 'http://sso.deliver.com:8087/xxl-sso-server'

router.beforeEach(async (to, from, next) => {

	// 检查 URL 是否带有 SSO token 参数
	const urlParams = new URLSearchParams(window.location.search)
	const ssoToken = urlParams.get('xxl_sso_sessionid')
	if (ssoToken) {
		localStorage.setItem('xxl_sso_sessionid', ssoToken)
		// 清除 URL 中的 token 参数
		window.history.replaceState(null, '', to.fullPath)
	}

	// 获取 token
	const token = localStorage.getItem('xxl_sso_sessionid')
	const redirectUrl = window.location.origin + to.fullPath
	
	if (!token) {
		// 没有 token 直接重定向到 SSO
		window.location.href = SSO_SERVER
		return
	}
	
    try {
        const response = await fetch('http://sso.deliver.com:8087/xxl-sso-server/app/logincheck', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ sessionId: token, redirectUrl: redirectUrl })
        });
        const data = await response.json();
        if (data.code === 200) {
            // 处理成功登录后的逻辑
            const store = useStore()
            store.setUsername(data.data.username)
			store.setUserId(data.data.userid)	
            next();
        } else if (data.code == 500) {
            window.location.href = SSO_SERVER;
        }
    } catch (error) {
        console.log(error);
        localStorage.removeItem('xxl_sso_sessionid');
        window.location.href = SSO_SERVER + '?redirect_url=' + redirectUrl;
    }

})

export default router
