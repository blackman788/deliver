<script lang="ts" setup>
import { ref } from 'vue'
import { ExclamationCircleOutlined, QuestionCircleOutlined, GithubOutlined } from '@ant-design/icons-vue'
import { useStore } from '@/store'	
const store = useStore()
const showAbout = ref(false)
const setShowAbout = (open: boolean): void => {
	showAbout.value = open
}
const handleLogout = async (): Promise<void> => {
	const token = localStorage.getItem('xxl_sso_sessionid')
	if (token) {
		localStorage.removeItem('xxl_sso_sessionid')
		try {
			const response = await fetch('http://sso.deliver.com:8087/xxl-sso-server/app/logout', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ sessionId: token })
			});
			const data = await response.json();
			if (data.code === 200) {
				// 处理成功登录后的逻辑
				window.location.href = data.redirectUrl;
			} 
		} catch (error) {
			console.log(error);
		}
	}
}

</script>
<template>
	<a-layout-header class="header">
		<div class="left">
			<a href="/">
				<img src="logo2.png" style="height: 35px; width: 40px; margin-top: 16px; margin-right: 8px" alt="空" />
			</a>
			<a href="/"><h1>Beacon 企业消息推送平台</h1></a>
		</div>
		<div class="right">
			<a-tooltip title="关于">
				<a style="color: #8b8b8b; font-size: 16px" @click="showAbout = true">
					<ExclamationCircleOutlined />
				</a>
			</a-tooltip>
			<a-modal
				v-model:open="showAbout"
				title="关于"
				centered
				@ok="setShowAbout(false)"
				:ok-button-props="{ disabled: true }"
				:cancel-button-props="{ disabled: true }"
				:footer="null">
				<div style="display: flex; margin-top: 20px">
					<div style="display: flex">
						<img src="logo2.png" style="width: 66px; height: 56px" alt="" />
						<h1 style="display: inline-block; height: 60px; line-height: 60px">Deliver</h1>
					</div>
					<div style="margin-left: 60px">
						<p>产品：Beacon 企业消息推送平台</p>
						<p>版本：v1.0.0</p>
					</div>

				</div>
			</a-modal>
			<a-tooltip title="疑问">
				<a target="_blank" href="#" style="color: #8b8b8b; font-size: 16px">
					<QuestionCircleOutlined />
				</a>
			</a-tooltip>
			<a-tooltip title="Gitee">
				<a href="#" target="_blank" style="color: #8b8b8b; font-size: 16px">
					<GithubOutlined />
				</a>
			</a-tooltip>
			<span class="avatar">
				<a-avatar style="width: 30px; height: 30px; font-size: 16px" src="mayi.png"></a-avatar>
				<a-dropdown>
					<span class="name">{{ store.username }}</span>
					<template #overlay>
						<a-menu>
							<a-menu-item key="1" @click="handleLogout">
								<span>退出登录</span>
							</a-menu-item>
						</a-menu>
					</template>
				</a-dropdown>
				
			</span>
		</div>
	</a-layout-header>
</template>
<style lang="scss" scoped>
.header {
	display: flex;
	justify-content: space-between;
	background-color: #fff;
	height: 60px;
	margin-left: -28px;
	margin-right: -28px;
	.left {
		display: flex;
		h1 {
			color: #000;
			font-size: 18px;
			height: 60px;
			line-height: 60px;
		}
	}

	.right {
		font-size: 20px;
		color: #8b8b8b;

		a {
			margin-right: 12px;
		}

		.avatar {
			display: inline-block;
			height: 100%;
			padding-right: 20px;
			padding-left: 20px;

			.name {
				margin-left: 10px;
				font-size: 15px;
			}
		}

		.avatar:hover {
			cursor: pointer;
			background-color: #f7f7f7;
		}
	}
}
</style>
