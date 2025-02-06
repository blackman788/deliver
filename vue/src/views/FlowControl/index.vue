<script lang="ts" setup>
import { onMounted, ref, watch, onUnmounted } from 'vue'
// import { useFlowControlStore } from '@/store/modules/flowControl'
import type { FlowControlData, FlowControlConfig } from './type'
import { Card, Row, Col, Statistic, Dropdown, Menu, Button, Modal, Form, Input, InputNumber, Switch } from 'ant-design-vue'
import * as echarts from 'echarts'
import type { MenuProps } from 'ant-design-vue'
import { getFlowControlConfigByCode, updateFlowControlConfig, getFlowControlData } from '@/api/flowControl'
import { message } from 'ant-design-vue'
// const flowControlStore = useFlowControlStore()
const flowControlData = ref<FlowControlData | null>(null)

// 初始化图表实例
const circuitBreakerChart = ref<HTMLElement>()
const responseTimeChart = ref<HTMLElement>()
const exceptionChart = ref<HTMLElement>()

let myCircuitBreakerChart: echarts.ECharts
let myResponseTimeChart: echarts.ECharts
let myExceptionChart: echarts.ECharts

// 定义颜色数组
const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#6d4c41']

// 模拟数据
const mockData: FlowControlData = {
  circuitBreaker: {
    failureRate: "15.5",
    slowCallRate: "8.3",
    numberOfFailedCalls: "31",
    numberOfSlowCalls: "16", 
    numberOfSuccessfulCalls: "153",
    state: "CLOSED"
  },
  rateLimiter: {
    availablePermissions: "85",
    numberOfWaitingThreads: "15"
  },
  timeLimiter: {
    timeoutCount: "23",
    avgResponseTime: "180",
    p95ResponseTime: "350",
    p99ResponseTime: "520",
    maxResponseTime: "800"
  },
  exceptions: {
    TimeoutException: 15,
    RuntimeException: 8,
    NullPointerException: 6,
    IOException: 12,
    SQLException: 3
  }
}

const fetchData = async () => {
  try {
    const channel = channelList.find(item => item.key === currentChannel.value)
    if (channel) {
      const { data } = await getFlowControlData(String(channel.value))
      flowControlData.value = data
      renderCharts()
      renderExceptionChart()
    }
  } catch (err) {
    console.error('获取流控数据失败:', err)
    // 只有在真正的错误时才显示错误提示
    if (err.response?.status !== 200) {
      message.error('获取流控数据失败')
    }
  }
}

// 渲染图表
const renderCharts = () => {
  if(!flowControlData.value || !circuitBreakerChart.value || !responseTimeChart.value) return
  
  myCircuitBreakerChart = echarts.init(circuitBreakerChart.value)
  myResponseTimeChart = echarts.init(responseTimeChart.value)

  // 检查断路器数据是否为空
  const hasCircuitBreakerData = flowControlData.value.circuitBreaker && 
    Object.keys(flowControlData.value.circuitBreaker).length > 0

  // 清除旧的图表配置
  myCircuitBreakerChart.clear()

  if (!hasCircuitBreakerData) {
    // 断路器空状态
    const emptyCircuitBreakerOption = {
      title: {
        text: '暂无断路器数据',
        x: 'center',
        y: 'center',
        textStyle: {
          color: '#999',
          fontSize: 14
        }
      }
    }
    myCircuitBreakerChart.setOption(emptyCircuitBreakerOption)
  } else {
    // 断路器饼图配置
    const circuitBreakerOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: [
            { 
              value: Number(flowControlData.value.circuitBreaker.numberOfSuccessfulCalls || 0), 
              name: '成功调用',
              itemStyle: { color: flowControlData.value.circuitBreaker.numberOfSuccessfulCalls ? '#3f8600' : '#d9d9d9' }
            },
            { 
              value: Number(flowControlData.value.circuitBreaker.numberOfFailedCalls || 0), 
              name: '失败调用',
              itemStyle: { color: flowControlData.value.circuitBreaker.numberOfFailedCalls ? '#cf1322' : '#d9d9d9' }
            },
            { 
              value: Number(flowControlData.value.circuitBreaker.numberOfSlowCalls || 0), 
              name: '慢调用',
              itemStyle: { color: flowControlData.value.circuitBreaker.numberOfSlowCalls ? '#faad14' : '#d9d9d9' }
            }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    myCircuitBreakerChart.setOption(circuitBreakerOption)
  }

  // 检查响应时间数据是否为空
  const hasResponseTimeData = flowControlData.value.timeLimiter && 
    flowControlData.value.timeLimiter.totalCount !== undefined &&
    flowControlData.value.timeLimiter.avgResponseTime !== undefined

  // 清除旧的图表配置
  myResponseTimeChart.clear()

  if (!hasResponseTimeData) {
    // 响应时间图表空状态
    const emptyResponseTimeOption = {
      title: {
        text: '暂无响应时间数据',
        x: 'center',
        y: 'center',
        textStyle: {
          color: '#999',
          fontSize: 14
        }
      }
    }
    myResponseTimeChart.setOption(emptyResponseTimeOption)
  } else {
    // 修改响应时间为柱状图配置
    const responseTimeOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: '{b}<br/>{a}: {c}ms'
      },
      legend: {
        data: ['平均响应时间', 'P95响应时间', 'P99响应时间', '最大响应时间']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        name: '毫秒',
        axisLabel: {
          formatter: '{value}ms'
        }
      },
      yAxis: {
        type: 'category',
        data: ['响应时间分布']
      },
      series: [
        {
          name: '平均响应时间',
          type: 'bar',
          data: [Number(flowControlData.value.timeLimiter.avgResponseTime)],
          itemStyle: { color: '#3f8600' },
          label: {
            show: true,
            position: 'right',
            formatter: '{c}ms'
          }
        },
        {
          name: 'P95响应时间',
          type: 'bar',
          data: [Number(flowControlData.value.timeLimiter.p95ResponseTime)],
          itemStyle: { color: '#096dd9' },
          label: {
            show: true,
            position: 'right',
            formatter: '{c}ms'
          }
        },
        {
          name: 'P99响应时间',
          type: 'bar',
          data: [Number(flowControlData.value.timeLimiter.p99ResponseTime)],
          itemStyle: { color: '#faad14' },
          label: {
            show: true,
            position: 'right',
            formatter: '{c}ms'
          }
        },
        {
          name: '最大响应时间',
          type: 'bar',
          data: [Number(flowControlData.value.timeLimiter.maxResponseTime)],
          itemStyle: { color: '#cf1322' },
          label: {
            show: true,
            position: 'right',
            formatter: '{c}ms'
          }
        }
      ]
    }
    myResponseTimeChart.setOption(responseTimeOption)
  }
}

// 渲染异常类型图表
const renderExceptionChart = () => {
  if(!flowControlData.value || !exceptionChart.value) return

  myExceptionChart = echarts.init(exceptionChart.value)

  // 检查异常数据是否为空
  const hasExceptionData = flowControlData.value.exceptions && 
    Object.keys(flowControlData.value.exceptions).length > 0

  // 清除旧的图表配置
  myExceptionChart.clear()

  if (!hasExceptionData) {
    const emptyOption = {
      title: {
        text: '暂无异常数据',
        x: 'center',
        y: 'center',
        textStyle: {
          color: '#999',
          fontSize: 14
        }
      }
    }
    myExceptionChart.setOption(emptyOption)
    return
  }

  // 将异常类型数据转换为数组
  const exceptionData = Object.entries(flowControlData.value.exceptions || {})
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10) // 只显示前10个异常

  const exceptionOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}<br/>{a}: {c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: exceptionData.map(item => item[0]),
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '异常数量',
        type: 'bar',
        barWidth: '60%',
        data: exceptionData.map(item => item[1]),
        itemStyle: {
          color: (params: any) => colors[params.dataIndex % colors.length] // 使用取模确保不会超出颜色数组范围
        }
      }
    ]
  }

  myExceptionChart.setOption(exceptionOption)
}

// 渠道列表
const channelList = [
  { key: '电话', value: 1, icon: '/assets/电话.png' },
  { key: '短信', value: 2, icon: '/assets/短信.png' },
  { key: '邮件', value: 3, icon: '/assets/邮件.png' },
  { key: '钉钉', value: 4, icon: '/assets/钉钉.png' },
  { key: '企微', value: 5, icon: '/assets/企业微信.png' },
  { key: '飞书', value: 6, icon: '/assets/飞书.png' }
]

// 当前选中的渠道
const currentChannel = ref('电话')

// 渠道切换事件
const handleChannelClick: MenuProps['onClick'] = ({ key }) => {
  currentChannel.value = key
  fetchData() // 获取新选中渠道的数据
}

// 编辑流控配置弹窗的可见性
const editModalVisible = ref(false)

// 表单实例
const editForm = ref()

// 表单数据
const formData = ref<FlowControlConfig>({
  rateLimiter: {
    limitForPeriod: '0',
    limitRefreshPeriodSeconds: '0',
    timeoutMillis: '0'
  },
  circuitBreaker: {
    failureRateThreshold: '0',
    slowCallRateThreshold: '0',
    waitDurationSeconds: '0',
    slowCallSeconds: '0',
    minimumNumberOfCalls: '0',
    slidingWindowSize: '0'
  },
  timeLimiter: {
    cancelRunningFuture: false,
    timeoutSeconds: '0'
  }
})

// 表单校验规则
const formRules = {
  'circuitBreaker.failureRateThreshold': [
    { required: true, message: '请输入失败率阈值' }
  ],
  'circuitBreaker.slowCallRateThreshold': [
    { required: true, message: '请输入慢调用率阈值' }
  ],
  'circuitBreaker.waitDurationSeconds': [
    { required: true, message: '请输入等待持续时间' }
  ],
  'circuitBreaker.slowCallSeconds': [
    { required: true, message: '请输入慢调用时间' }
  ],
  'circuitBreaker.minimumNumberOfCalls': [
    { required: true, message: '请输入最小调用次数' }
  ],
  'circuitBreaker.slidingWindowSize': [
    { required: true, message: '请输入滑动窗口大小' }
  ],
  'rateLimiter.limitForPeriod': [
    { required: true, message: '请输入周期内限制调用次数' }
  ],
  'rateLimiter.limitRefreshPeriodSeconds': [
    { required: true, message: '请输入限制周期刷新时间' }
  ],
  'rateLimiter.timeoutMillis': [
    { required: true, message: '请输入超时时间' }
  ],
  'timeLimiter.timeoutSeconds': [
    { required: true, message: '请输入超时时间' }
  ]
}

// 打开编辑弹窗
const handleEditConfig = async () => {
  try {
    const channel = channelList.find(item => item.key === currentChannel.value)
    if (channel) {
      const { data: response } = await getFlowControlConfigByCode(String(channel.value))
      // 更新表单数据，确保正确转换 cancelRunningFuture 的值
      formData.value = {
        rateLimiter: {
          limitForPeriod: response.rateLimiter.limitForPeriod,
          limitRefreshPeriodSeconds: response.rateLimiter.limitRefreshPeriodSeconds,
          timeoutMillis: response.rateLimiter.timeoutMillis
        },
        circuitBreaker: {
          failureRateThreshold: response.circuitBreaker.failureRateThreshold,
          slowCallRateThreshold: response.circuitBreaker.slowCallRateThreshold,
          waitDurationSeconds: response.circuitBreaker.waitDurationSeconds,
          slowCallSeconds: response.circuitBreaker.slowCallSeconds,
          minimumNumberOfCalls: response.circuitBreaker.minimumNumberOfCalls,
          slidingWindowSize: response.circuitBreaker.slidingWindowSize
        },
        timeLimiter: {
          // 使用 Boolean 函数确保转换为布尔值
          cancelRunningFuture: response.timeLimiter.cancelRunningFuture,
          timeoutSeconds: response.timeLimiter.timeoutSeconds
        }
      }
      editModalVisible.value = true
    }
  } catch (error) {
    console.error('获取流控配置失败:', error)
    message.error('获取流控配置失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    const values = await editForm.value?.validateFields()
    console.log('表单值:', values)

    const channel = channelList.find(item => item.key === currentChannel.value)
    if (channel) {
      const requestData: FlowControlConfig = {
        rateLimiter: {
          limitForPeriod: String(values.rateLimiter.limitForPeriod),
          limitRefreshPeriodSeconds: String(values.rateLimiter.limitRefreshPeriodSeconds),
          timeoutMillis: String(values.rateLimiter.timeoutMillis)
        },
        circuitBreaker: {
          failureRateThreshold: String(values.circuitBreaker.failureRateThreshold),
          slowCallRateThreshold: String(values.circuitBreaker.slowCallRateThreshold),
          waitDurationSeconds: String(values.circuitBreaker.waitDurationSeconds),
          slowCallSeconds: String(values.circuitBreaker.slowCallSeconds),
          minimumNumberOfCalls: String(values.circuitBreaker.minimumNumberOfCalls),
          slidingWindowSize: String(values.circuitBreaker.slidingWindowSize)
        },
        timeLimiter: {
          // 转换为字符串 'true' 或 'false'
          cancelRunningFuture: String(values.timeLimiter.cancelRunningFuture),
          timeoutSeconds: String(values.timeLimiter.timeoutSeconds)
        }
      }

      await updateFlowControlConfig(String(channel.value), requestData)
      message.success('配置更新成功')
      editModalVisible.value = false
    }
  } catch (error) {
    console.error('表单校验失败或更新失败:', error)
    message.error('配置更新失败')
  }
}

// 取消编辑
const handleCancel = () => {
  editForm.value?.resetFields()
  editModalVisible.value = false
}

// 修改定时器类型
let timer: number | null = null

onMounted(() => {
  fetchData()
  // 每30秒刷新一次数据
  timer = setInterval(() => {
    fetchData()
  }, 30000)
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})

// 监听 flowControlData 的变化,重新渲染图表
watch(flowControlData, () => {
  renderCharts()
  renderExceptionChart()
})
</script>

<template>
  <div class="flow-control-dashboard">
    <div class="dashboard-header">
      <div class="channel-select">
        <Dropdown>
          <Button>
            <img :src="channelList.find(item => item.key === currentChannel)?.icon" class="channel-icon" />
            {{ currentChannel }}
            <DownOutlined />
          </Button>
          <template #overlay>
            <Menu @click="handleChannelClick">
              <Menu.Item v-for="channel in channelList" :key="channel.key">
                <img style="height: 16px; width: 16px" :src="channel.icon" class="channel-icon menu-icon" />
                {{ channel.key }}
              </Menu.Item>
            </Menu>
          </template>
        </Dropdown>
      </div>
      <!-- 添加编辑流控配置按钮 -->
      <div class="edit-config-button">
        <Button type="primary" @click="handleEditConfig">编辑流控配置</Button>
      </div>
    </div>

    <!-- 断路器数据 -->
    <Card title="断路器监控" class="dashboard-card">
      <Row :gutter="[16, 16]">
        <Col :span="12">
          <div ref="circuitBreakerChart" style="width: 100%; height: 300px"></div>
        </Col>
        <Col :span="12">
          <Row :gutter="[16, 16]">
            <Col :span="12">
              <Statistic 
                title="失败率" 
                :value="flowControlData?.circuitBreaker?.failureRate || '0'" 
                :precision="2"
                suffix="%" 
              />
            </Col>
            <Col :span="12">
              <Statistic 
                title="慢调用比例" 
                :value="flowControlData?.circuitBreaker?.slowCallRate || '0'" 
                :precision="2"
                suffix="%" 
              />
            </Col>
            <Col :span="24">
              <Statistic 
                title="熔断器状态" 
                :value="flowControlData?.circuitBreaker?.state || 'CLOSED'"
                :value-style="{ color: (flowControlData?.circuitBreaker?.state || 'CLOSED') === 'CLOSED' ? '#3f8600' : '#cf1322' }"
              />
            </Col>
          </Row>
        </Col>
      </Row>
    </Card>

    <!-- 限流器数据 -->
    <Card title="限流器监控" class="dashboard-card">
      <Row :gutter="[16, 16]">
        <Col :span="12">
          <Statistic 
            title="可执行请求数" 
            :value="flowControlData?.rateLimiter?.availablePermissions || '0'"
            :value-style="{ color: '#3f8600' }"
          />
        </Col>
        <Col :span="12">
          <Statistic 
            title="等待请求数" 
            :value="flowControlData?.rateLimiter?.numberOfWaitingThreads || '0'"
            :value-style="{ color: '#cf1322' }"
          />
        </Col>
      </Row>
    </Card>

    <!-- 修改超时监控卡片的布局 -->
    <Card title="超时监控" class="dashboard-card">
      <Row :gutter="[16, 16]">
        <Col :span="24">
          <Statistic 
            title="总调用次数" 
            :value="flowControlData?.timeLimiter?.totalCount || '0'"
            :value-style="{ color: '#cf1322', fontSize: '24px' }"
          />
        </Col>
        <Col :span="24">
          <div ref="responseTimeChart" style="width: 100%; height: 300px"></div>
        </Col>
      </Row>
    </Card>

    <!-- 修改异常类型统计卡片 -->
    <Card title="异常类型统计" class="dashboard-card">
      <div ref="exceptionChart" style="width: 100%; height: 300px;"></div>
    </Card>

    <!-- 编辑流控配置弹窗 -->
    <Modal
      v-model:visible="editModalVisible"
      title="编辑流控配置"
      @ok="handleSubmit"
      @cancel="handleCancel"
      width="600px"
      :maskClosable="false"
      :keyboard="false"
      :destroyOnClose="true"
    >
      <Form 
        ref="editForm" 
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 8 }" 
        :wrapper-col="{ span: 16 }"
      >
        <div class="config-section">
          <div class="section-title">断路器配置</div>
          <Form.Item
            label="失败率阈值"
            :name="['circuitBreaker', 'failureRateThreshold']"
            :rules="formRules['circuitBreaker.failureRateThreshold']"
          >
            <InputNumber
              v-model:value="formData.circuitBreaker.failureRateThreshold"
              class="custom-input-number"
              :min="0"
              :max="100"
              :precision="2"
              addon-after="%"
              :controls="true"
            />
          </Form.Item>
          <Form.Item
            label="慢调用率阈值"
            :name="['circuitBreaker', 'slowCallRateThreshold']"
            :rules="formRules['circuitBreaker.slowCallRateThreshold']"
          >
            <InputNumber
              v-model:value="formData.circuitBreaker.slowCallRateThreshold"
              class="custom-input-number"
              :min="0"
              :max="100"
              :precision="2"
              addon-after="%"
              :controls="true"
            />
          </Form.Item>
          <Form.Item
            label="等待持续时间"
            :name="['circuitBreaker', 'waitDurationSeconds']"
            :rules="formRules['circuitBreaker.waitDurationSeconds']"
          >
            <InputNumber
              v-model:value="formData.circuitBreaker.waitDurationSeconds"
              class="custom-input-number"
              :min="0"
              addon-after="秒"
              :controls="true"
            />
          </Form.Item>
          <Form.Item
            label="慢调用时间"
            :name="['circuitBreaker', 'slowCallSeconds']"
            :rules="formRules['circuitBreaker.slowCallSeconds']"
          >
            <InputNumber
              v-model:value="formData.circuitBreaker.slowCallSeconds"
              class="custom-input-number"
              :min="0"
              addon-after="秒"
              :controls="true"
            />
          </Form.Item>
          <Form.Item
            label="最小调用次数"
            :name="['circuitBreaker', 'minimumNumberOfCalls']"
            :rules="formRules['circuitBreaker.minimumNumberOfCalls']"
          >
            <InputNumber
              v-model:value="formData.circuitBreaker.minimumNumberOfCalls"
              class="custom-input-number"
              :min="0"
              :controls="true"
            />
          </Form.Item>
          <Form.Item
            label="滑动窗口大小"
            :name="['circuitBreaker', 'slidingWindowSize']"
            :rules="formRules['circuitBreaker.slidingWindowSize']"
          >
            <InputNumber
              v-model:value="formData.circuitBreaker.slidingWindowSize"
              class="custom-input-number"
              :min="0"
              :controls="true"
            />
          </Form.Item>
        </div>

        <div class="config-section">
          <div class="section-title">限流器配置</div>
          <Form.Item
            label="周期内限制调用次数"
            :name="['rateLimiter', 'limitForPeriod']"
            :rules="formRules['rateLimiter.limitForPeriod']"
          >
            <InputNumber
              v-model:value="formData.rateLimiter.limitForPeriod"
              class="custom-input-number"
              :min="0"
              :controls="true"
            />
          </Form.Item>
          <Form.Item
            label="限制周期刷新时间"
            :name="['rateLimiter', 'limitRefreshPeriodSeconds']"
            :rules="formRules['rateLimiter.limitRefreshPeriodSeconds']"
          >
            <InputNumber
              v-model:value="formData.rateLimiter.limitRefreshPeriodSeconds"
              class="custom-input-number"
              :min="0"
              addon-after="秒"
              :controls="true"
            />
          </Form.Item>
          <Form.Item
            label="超时时间"
            :name="['rateLimiter', 'timeoutMillis']"
            :rules="formRules['rateLimiter.timeoutMillis']"
          >
            <InputNumber
              v-model:value="formData.rateLimiter.timeoutMillis"
              class="custom-input-number"
              :min="0"
              addon-after="毫秒"
              :controls="true"
            />
          </Form.Item>
        </div>

        <div class="config-section">
          <div class="section-title">超时器配置</div>
          <Form.Item
            label="超时时间"
            :name="['timeLimiter', 'timeoutSeconds']"
            :rules="formRules['timeLimiter.timeoutSeconds']"
          >
            <InputNumber
              v-model:value="formData.timeLimiter.timeoutSeconds"
              class="custom-input-number"
              :min="0"
              addon-after="秒"
              :controls="true"
            />
          </Form.Item>
          <Form.Item
            label="是否取消正在运行的任务"
            :name="['timeLimiter', 'cancelRunningFuture']"
            valuePropName="checked"
          >
            <Switch v-model:checked="formData.timeLimiter.cancelRunningFuture" />
          </Form.Item>
        </div>
      </Form>
    </Modal>
  </div>
</template>

<style lang="scss" scoped>
.flow-control-dashboard {
  padding: 24px;
  background: #f0f2f5;

  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .channel-select {
      .channel-icon {
        width: 16px;
        height: 16px;
        margin-right: 8px;
      }

      :deep(.menu-icon) {
        width: 16px;
        height: 16px;
        margin-right: 8px;
      }
    }

    .edit-config-button {
      margin-left: 16px;
    }
  }

  .dashboard-card {
    margin-bottom: 24px;
    
    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f0f0;
      padding: 0 24px;
    }

    :deep(.ant-card-body) {
      padding: 24px;
    }
  }

  .ant-statistic {
    text-align: center;
  }
}

.config-section {
  margin-bottom: 24px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.85);
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }

  &:last-child {
    margin-bottom: 0;
  }
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}

:deep(.ant-modal-body) {
  padding: 24px;
  max-height: 600px;
  overflow-y: auto;
}

:deep(.custom-input-number) {
  width: 200px !important;
}

:deep(.ant-input-number-group-wrapper) {
  width: 200px !important;
}
</style> 