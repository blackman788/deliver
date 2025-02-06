<script lang="ts" setup>
import { ref, reactive, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { saveReceiver, updateReceiver } from '@/api/receiver'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '新增接收方'
  },
  record: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:open', 'success'])

const formRef = ref<FormInstance>()
const confirmLoading = ref<boolean>(false)

const formState = reactive({
  sender: '',
  password: '',
  encryption: undefined as string | undefined,
  secret: ''
})

// 监听编辑时的数据
watch(
  () => props.open,
  (val) => {
    if (val) {
      // 打开弹窗时
      if (props.record && Object.keys(props.record).length > 0) {
        // 编辑模式：设置表单数据
        formState.sender = props.record.sender || ''
        formState.password = props.record.password || ''  // 保留原密码
        formState.encryption = props.record.encryption?.toString()
        formState.secret = props.record.secret || ''
      } else {
        // 新增模式：重置表单
        formState.sender = ''
        formState.password = ''
        formState.encryption = undefined
        formState.secret = ''
      }
    }
  }
)

const rules = {
  sender: [{ required: true, message: '请输入发送方名称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  encryption: [{ required: true, message: '请选择加密方式', trigger: 'change' }]
}

const handleOk = async () => {
  try {
    await formRef.value?.validate()
    confirmLoading.value = true

    // 直接使用表单数据，不进行加密
    const requestData = {
      ...formState,
      encryption: String(formState.encryption)
    }
    
    // 根据是否有 id 判断是新增还是编辑
    if (props.record?.id) {
      await updateReceiver({
        ...requestData,
        id: props.record.id
      })
    } else {
      await saveReceiver(requestData)
    }
    
    message.success('操作成功')
    emit('success')
    handleCancel()
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败：' + (error instanceof Error ? error.message : String(error)))
  } finally {
    confirmLoading.value = false
  }
}

const handleCancel = () => {
  emit('update:open', false)
  formRef.value?.resetFields()
}

// 修改生成密钥的函数
const generateKey = () => {
  const length = 16 // 与 "1234567890abcdef" 长度相同
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+' // 包含数字、字母和特殊字符
  let key = ''

  for (let i = 0; i < length; i++) {
    // 随机选择一个字符
    key += characters.charAt(Math.floor(Math.random() * characters.length))
  }

  formState.secret = key
}
</script>

<template>
  <a-modal
    :title="title"
    :open="open"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="发送方" name="sender">
        <a-input v-model:value="formState.sender" placeholder="请输入发送方名称" />
      </a-form-item>
      <a-form-item label="密码" name="password">
        <a-input-password v-model:value="formState.password" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item label="加密方式" name="encryption">
        <a-select
          v-model:value="formState.encryption"
          placeholder="请选择加密方式"
          :options="[
            { value: '0', label: '不加密' },
            { value: '1', label: 'AES加密' },
            { value: '2', label: 'MD5加密' }
          ]"
        />
      </a-form-item>
      <a-form-item 
        label="密钥" 
        name="secret"
        v-if="formState.encryption === '1'"
      >
        <a-input-search
          v-model:value="formState.secret"
          placeholder="点击生成密钥"
          enter-button="生成密钥"
          @search="generateKey"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<style lang="scss" scoped>
:deep(.ant-input-search .ant-input-group-addon) {
  .ant-btn {
    padding: 0 15px;
  }
}
</style> 