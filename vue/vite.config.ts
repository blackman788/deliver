import { defineConfig } from 'vite'
import eslint from 'vite-plugin-eslint' // 新增
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
// 按需导入ant design vue
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
import { prismjsPlugin } from 'vite-plugin-prismjs'
export default defineConfig({
	server: {
		host: 'localhost',
		port: 8080, // 端口
		proxy: {
			'/backend': {
				// 请求接口中要替换的标识
				target: 'http://bak.deliver.com:9091/admin', // 代理地址
				changeOrigin: true, // 是否允许跨域
				secure: true,
				rewrite: (path) => path.replace(/^\/backend/, '')
			},
			'/sso': {
				target: 'http://sso.deliver.com:8087/xxl-sso-server/', // 代理地址
				changeOrigin: true, // 是否允许跨域
				secure: true,
				rewrite: (path) => path.replace(/^\/sso/, '')
			}
		}
	},
	plugins: [
		vue(),
		eslint({cache: false}),
		Components({
			resolvers: [
				AntDesignVueResolver({
					importStyle: false // css in js
				})
			]
		}),
		prismjsPlugin({
			languages: ['json', 'js'],
			plugins: ['line-numbers', 'copy-to-clipboard'],
			theme: 'solarizedlight',
			css: true
		})
	
	],
	resolve: {
		alias: {
			'@': resolve(__dirname, './src'),
			'*': resolve('')
		}
	}
})
