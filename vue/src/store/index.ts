import { defineStore } from 'pinia'

export const useStore = defineStore('store', {
	state: () => {
		return {
			collapse: false,
			showBannerFlag: true,
			username:'',
			userId:''
		}
	},
	getters: {},
	actions: {
		getCollapse() {
			return this.collapse
		},
		changeCollapse() {
			this.collapse = !this.collapse
		},
		closeBanner() {
			this.showBannerFlag = false
		},
		setUsername(username: string) {
			console.log(username)
			this.username = username
		},
		setUserId(userId: string) {
			this.userId = userId
		}
	}
})
