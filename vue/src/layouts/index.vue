<script lang="ts" setup>
import Header from './components/Header/index.vue'
import Banner from '../components/Banner/index.vue'
import Section from './components/Section/index.vue'
</script>

<template>
	<a-layout style="min-height: 100%">
		<div style="position: fixed; z-index: 999; width: 100%; border-bottom: 1px solid #ececec">
			<Banner></Banner>
			<Header></Header>
		</div>
		<a-layout>
			<Section></Section>
		</a-layout>
	</a-layout>
</template>
<style scoped lang="scss"></style>
