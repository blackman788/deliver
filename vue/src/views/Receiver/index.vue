<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  PlusOutlined, 
  ReloadOutlined, 
  SettingOutlined,
  EditOutlined,
  DeleteOutlined 
} from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { h } from 'vue'
import { message } from 'ant-design-vue'
import { getPageReceiver, deleteReceiver } from '@/api/receiver'
import type { searchReceiver } from './type'
import ReceiverModal from './components/ReceiverModal.vue'
import CryptoJS from 'crypto-js'
import { TypographyParagraph } from 'ant-design-vue'

// 添加加密方式映射
const encryptionMap = {
  '0': { text: '不加密', color: 'default' },
  '1': { text: 'AES', color: 'blue' },
  '2': { text: 'MD5', color: 'green' }
} as const

// 修改加密函数
const encryptPassword = (password: string, type: string, secret?: string) => {
  switch (type) {
    case '1': { // AES
      if (!secret) {
        throw new Error('AES加密需要密钥')
      }
      const mode = "ECB"
      const pad = "Pkcs7"
      const keyType = "Utf8"
      const iv = ""
      const ivType = "Utf8"
      const isBase64 = false

      const crypto_key = CryptoJS.enc[keyType].parse(secret)

      let cfg = {}
      mode !== "ECB" && (cfg.iv = CryptoJS.enc[ivType].parse(iv))
      cfg.mode = CryptoJS.mode[mode]
      cfg.padding = CryptoJS.pad[pad]

      return CryptoJS.AES.encrypt(password, crypto_key, cfg).ciphertext.toString(isBase64 ? CryptoJS.enc.Base64 : CryptoJS.enc.Hex)
    }
    case '2': // MD5
      return CryptoJS.MD5(password).toString()
    default:
      return '无'
  }
}

const columns: TableColumnsType = [
  {
    title: '序号',
    key: 'index',
    width: '10%',
    align: 'center',
    customRender: ({ index }) => index + 1 + (current.value - 1) * pageSize.value
  },
  {
    title: '发送方',
    dataIndex: 'sender',
    key: 'sender',
    align: 'center',
    width: '15%'
  },
  {
    title: '密码',
    dataIndex: 'password',
    key: 'password',
    align: 'center',
    width: '18%',
    ellipsis: {
      showTitle: true
    }
  },
  {
    title: '加密后密码',
    dataIndex: 'encryptedPassword',
    key: 'encryptedPassword',
    align: 'center',
    width: '18%',
    customRender: ({ text }) => {
      return h('div', { class: 'copy-content' }, [
        h(TypographyParagraph, {
          content: text,
          ellipsis: { rows: 1, tooltip: true },
          copyable: {
            text: text,
            onCopy: () => copyToClipboard(text, '加密后密码')
          }
        })
      ])
    }
  },
  {
    title: '加密方式',
    dataIndex: 'encryption',
    key: 'encryption',
    align: 'center',
    width: '10%'
  },
  {
    title: '密钥',
    dataIndex: 'secret',
    key: 'secret',
    align: 'center',
    width: '22%',
    ellipsis: {
      showTitle: true
    }
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    align: 'center',
    width: '15%'
  },
  {
    title: '操作',
    key: 'operation',
    fixed: false,
    width: '15%',
    align: 'center'
  }
]

const dataSource = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)

// 分页配置
const locale = {
  items_per_page: '条/页',
  jump_to: '跳至',
  page: '页',
  prev_page: '上一页',
  next_page: '下一页'
}

// 添加弹窗相关的状态
const modalVisible = ref(false)
const modalTitle = ref('新增接收方')
const currentRecord = ref({})

// 搜索表单数据
const searchForm = reactive<searchReceiver>({
  sender: undefined,
  encryption: undefined,
  currentPage: 1,
  pageSize: 10
})

// 添加复制函数
const copyToClipboard = (text: string, type: string) => {
  try {
    // 创建临时文本区域
    const textArea = document.createElement('textarea')
    textArea.value = text
    // 将文本区域添加到文档中
    document.body.appendChild(textArea)
    // 选择文本
    textArea.select()
    // 执行复制命令
    document.execCommand('copy')
    // 移除临时文本区域
    document.body.removeChild(textArea)
    // 提示成功
    message.success(`${type}已复制到剪贴板`)
  } catch (err) {
    message.error('复制失败，请手动复制')
  }
}

// 获取列表数据
const getReceiverList = async () => {
  loading.value = true
  try {
    const res = await getPageReceiver({
      ...searchForm,
      currentPage: current.value,
      pageSize: pageSize.value
    })
    dataSource.value = res.data.records.map(item => ({
      ...item,
      encryptedPassword: encryptPassword(item.password, item.encryption, item.secret)
    }))
    total.value = res.data.total
    message.success('查询成功')
  } catch (error) {
    message.error('查询失败：' + error)
  } finally {
    loading.value = false
  }
}

// 分页变化
const handleTableChange = (page: number, size: number) => {
  current.value = page
  pageSize.value = size
  getReceiverList()
}

// 搜索
const handleSearch = () => {
  current.value = 1
  getReceiverList()
}

// 重置搜索
const handleReset = () => {
  searchForm.sender = undefined
  searchForm.encryption = undefined
  current.value = 1
  getReceiverList()
}

// 删除
const handleDelete = async (record: any) => {
  try {
    await deleteReceiver({ id: record.id })
    message.success('删除成功')
    getReceiverList()
  } catch (error) {
    message.error('删除失败：' + error)
  }
}

// 打开新增弹窗
const handleAdd = () => {
  modalTitle.value = '新增接收方'
  currentRecord.value = {}
  modalVisible.value = true
}

// 打开编辑弹窗
const handleEdit = (record: any) => {
  modalTitle.value = '编辑接收方'
  currentRecord.value = { ...record }
  modalVisible.value = true
}

// 处理弹窗成功提交
const handleSuccess = () => {
  getReceiverList()
}

onMounted(() => {
  getReceiverList()
})
</script>

<template>
  <div id="receiver-container">
    <!-- 搜索部分 -->
    <div class="search-section">
      <a-form layout="inline">
        <a-form-item label="发送方">
          <a-input
            v-model:value="searchForm.sender"
            placeholder="请输入发送方"
            allowClear
          />
        </a-form-item>
        <a-form-item label="加密方式">
          <a-select
            v-model:value="searchForm.encryption"
            placeholder="请选择加密方式"
            style="width: 200px"
            allowClear
          >
            <a-select-option value="0">不加密</a-select-option>
            <a-select-option value="1">AES加密</a-select-option>
            <a-select-option value="2">MD5加密</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 表格部分 -->
    <section>
      <div class="splitter">
        <a-tooltip title="刷新">
          <a-button shape="circle" :icon="h(ReloadOutlined)" @click="getReceiverList" />
        </a-tooltip>
        <a-button type="primary" @click="handleAdd">
          <template #icon>
            <PlusOutlined />
          </template>
          新增接收方
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="false"
        bordered
      >
        <template #headerCell="{ column }">
          <template v-if="column.key === 'operation'">
            <span>
              <SettingOutlined />
              操作
            </span>
          </template>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'password'">
            <div class="copy-content">
              <a-typography-paragraph
                :content="record.password"
                :ellipsis="{ rows: 1, tooltip: true }"
                :copyable="{
                  text: record.password,
                  onCopy: () => copyToClipboard(record.password, '密码')
                }"
              />
            </div>
          </template>
          <template v-else-if="column.key === 'secret'">
            <div class="copy-content">
              <a-typography-paragraph
                :content="record.secret"
                :ellipsis="{ rows: 1, tooltip: true }"
                :copyable="{
                  text: record.secret,
                  onCopy: () => copyToClipboard(record.secret, '密钥')
                }"
              />
            </div>
          </template>
          <template v-else-if="column.key === 'encryption'">
            <a-tag :color="encryptionMap[record.encryption]?.color">
              {{ encryptionMap[record.encryption]?.text || record.encryption }}
            </a-tag>
          </template>
          <template v-if="column.key === 'operation'">
            <div class="operation-btns">
              <a-tooltip title="编辑">
                <EditOutlined class="operation-icon" @click="handleEdit(record)" />
              </a-tooltip>
              <a-tooltip title="删除">
                <a-popconfirm
                  title="确定要删除吗？"
                  @confirm="() => handleDelete(record)"
                >
                  <DeleteOutlined class="operation-icon delete-icon" />
                </a-popconfirm>
              </a-tooltip>
            </div>
          </template>
        </template>
      </a-table>

      <a-pagination
        v-model:current="current"
        v-model:pageSize="pageSize"
        class="pagination"
        show-quick-jumper
        :total="total"
        @change="handleTableChange"
        showSizeChanger
        :locale="locale"
        :show-total="(total) => `共 ${total} 条数据`"
      />
    </section>

    <!-- 添加弹窗组件 -->
    <ReceiverModal
      v-model:open="modalVisible"
      :title="modalTitle"
      :record="currentRecord"
      @success="handleSuccess"
    />
  </div>
</template>

<style lang="scss" scoped>
#receiver-container {
  position: relative;
  width: 100%;
  overflow: auto;

  .search-section {
    padding: 24px;
    background: #fff;
    border-radius: 6px;
    margin-bottom: 12px;
  }

  section {
    padding: 12px;
    margin-top: 12px;
    background: #fff;
    border-radius: 6px;

    .splitter {
      display: flex;
      align-items: center;
      justify-content: right;
      width: 100%;
      height: 60px;
      margin-bottom: 6px;
    }

    .pagination {
      display: flex;
      justify-content: right;
      margin-top: 20px;
    }

    :deep(.ant-table) {
      .ant-table-thead > tr > th {
        background-color: #fafafa;
        padding: 8px 16px;
        &::before {
          display: none;
        }
      }

      .ant-table-tbody > tr > td {
        padding: 8px 16px;
      }

      .operation-btns {
        display: flex;
        justify-content: center;
        gap: 16px;

        .operation-icon {
          font-size: 16px;
          cursor: pointer;
          transition: color 0.3s;
          color: #1677ff;

          &:hover {
            color: #4096ff;
          }

          &.delete-icon {
            color: #ff4d4f;

            &:hover {
              color: #ff7875;
            }
          }
        }
      }
    }
  }
}

.copy-content {
  :deep(.ant-typography) {
    margin-bottom: 0;
    
    .ant-typography-copy {
      color: #1677ff;
      
      &:hover {
        color: #4096ff;
      }
    }

    .ant-typography-content {
      display: inline-block;
      max-width: 100%;
      vertical-align: bottom;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}
</style> 