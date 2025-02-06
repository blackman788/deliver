<script lang="ts" setup>
import { useStore } from '@/store'
import { CloseOutlined } from '@ant-design/icons-vue'
const store = useStore()
</script>
<template>
	<div
		v-if="store.$state.showBannerFlag"
		data-show="true"
		role="alert"
		style="
			width: 100%;
			padding: 0;
			background: repeating-linear-gradient(35deg, rgb(179 235 255), rgb(179 235 255) 20px, rgb(230 248 255) 10px, rgb(230 248 255) 40px);
		">
		<div style="overflow: hidden; color: black; text-align: center; text-overflow: ellipsis; white-space: nowrap">
			<span>
				⭐️ Beacon 是一个面向企业的全面消息推送平台，旨在提供企业内部沟通和协作的便捷解决方案。
				 它以轻量级部署、简单易用、支持多种通信渠道为特点，为企业提供高效的消息传递和通知功能。👏🏻
				<a-tooltip title="关闭">
					<a style="display: inline-block; position: absolute; right: 10px" @click="store.closeBanner">
						<CloseOutlined style="color: #5d5b5b" />
					</a>
				</a-tooltip>
			</span>
		</div>
	</div>
</template>
<style lang="scss" scoped>
span {
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 14px;
	line-height: 25px;
	text-align: center;
}

.ico {
	width: 25px;
	height: 25px;
}
</style>
