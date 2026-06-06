<template>
  <div class="owner-payment">
    <div class="header-bar">
      <h2>缴费通知</h2>
      <el-radio-group v-model="filter" size="small" @change="loadData">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="unpaid">待缴费</el-radio-button>
        <el-radio-button label="paid">已缴费</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 待缴费卡片列表 -->
    <div v-if="list.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无缴费通知" />
    </div>

    <el-row :gutter="16">
      <el-col
        :span="8"
        v-for="item in list"
        :key="item.id"
        style="margin-bottom: 16px;"
      >
        <el-card shadow="hover" :class="['payment-card', item.status]">
          <div class="card-top">
            <el-tag :type="item.status === 'paid' ? 'success' : 'danger'" size="small">
              {{ item.status === 'paid' ? '已缴费' : '待缴费' }}
            </el-tag>
            <span class="fee-type">{{ item.feeTypeName }}</span>
          </div>

          <div class="amount">¥{{ item.amount?.toFixed(2) }}</div>

          <div class="info-list">
            <div class="info-item">
              <span class="label">截止日期：</span>
              <span>{{ formatDate(item.deadline) }}</span>
            </div>
            <div class="info-item" v-if="item.description">
              <span class="label">描述：</span>
              <span>{{ item.description }}</span>
            </div>
            <div class="info-item" v-if="item.payTime">
              <span class="label">缴费时间：</span>
              <span>{{ formatDate(item.payTime) }}</span>
            </div>
          </div>

          <div class="card-footer" v-if="item.status === 'unpaid'">
            <el-button
              type="primary"
              size="small"
              @click="handlePay(item)"
              :loading="payingId === item.id"
            >
              立即缴费
            </el-button>
          </div>

          <div class="card-footer paid-footer" v-else>
            <el-icon><CircleCheck /></el-icon>
            <span>已完成</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
const list = ref([])
const loading = ref(false)
const payingId = ref(null)
const filter = ref('all')
const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  console.log('📌 当前 token:', token)  // ✅ 调试：看 token 是否存在
  return { headers: { token } }
}
const loadData = async () => {
  loading.value = true
  try {
    console.log('📡 开始请求 /api/owner/paymentNotifications')
    const res = await axios.get('/api/owner/paymentNotifications', getHeaders())
    
    // ===== 🔍 详细调试 =====
    console.log('========== 响应调试 ==========')
    console.log('1. res 类型:', typeof res)
    console.log('2. res.status:', res.status)
    console.log('3. res.data 类型:', typeof res.data)
    console.log('4. res.data:', res.data)
    
    // 判断 res.data 是字符串还是对象
    let responseData = res.data
    if (typeof res.data === 'string') {
      console.log('⚠️ res.data 是字符串，尝试解析...')
      try {
        responseData = JSON.parse(res.data)
        console.log('5. 解析后:', responseData)
      } catch (parseError) {
        console.error('❌ JSON 解析失败:', parseError)
      }
    }
    
    console.log('6. responseData:', responseData)
    console.log('7. responseData.code:', responseData.code)
    console.log('8. responseData.data:', responseData.data)
    console.log('9. responseData.data 长度:', responseData.data?.length)
    console.log('==============================')
    
    if (responseData.code === 200) {
      let data = responseData.data || []
      console.log('10. 原始数据:', data)
      
      // 前端根据筛选条件过滤
      if (filter.value === 'unpaid') {
        data = data.filter(item => item.status === 'unpaid')
        console.log('11. 过滤 unpaid 后:', data)
      } else if (filter.value === 'paid') {
        data = data.filter(item => item.status === 'paid')
        console.log('11. 过滤 paid 后:', data)
      }
      
      list.value = data
      console.log('✅ 最终 list:', list.value)
    } else {
      console.log('❌ 错误响应码:', responseData.code, '消息:', responseData.msg)
      ElMessage.error(responseData.msg || '获取数据失败')
    }
  } catch (e) {
    console.error('❌ 请求异常:', e)
    console.error('异常详情:', e.response || e.message)
    ElMessage.error('获取缴费通知失败')
  } finally {
    loading.value = false
  }
}
const handlePay = (item) => {
  ElMessageBox.confirm(
    `确认缴纳「${item.feeTypeName}」¥${item.amount?.toFixed(2)} ？`,
    '确认缴费',
    {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    payingId.value = item.id
    try {
      console.log('💰 开始支付:', item.id)
      const res = await axios.put(`/api/owner/pay/${item.id}`, {}, getHeaders())
      
      console.log('支付响应:', res.data)
      
      if (res.data.code === 200) {
        ElMessage.success('缴费成功')
        loadData()
      } else {
        ElMessage.error(res.data.msg || '缴费失败')
      }
    } catch (e) {
      console.error('支付异常:', e)
      ElMessage.error('缴费失败')
    } finally {
      payingId.value = null
    }
  }).catch(() => {})
}
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ')
}
onMounted(() => {
  console.log('🚀 组件已挂载，开始加载数据')
  loadData()
})
</script>
<style scoped>
.owner-payment {
  padding: 4px;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-bar h2 {
  margin: 0;
  font-size: 20px;
}

.empty-state {
  margin-top: 80px;
}

.payment-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.payment-card:hover {
  transform: translateY(-2px);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.fee-type {
  font-weight: bold;
  color: #303133;
}

.amount {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
  text-align: center;
  margin: 16px 0;
}

.payment-card.paid .amount {
  color: #67c23a;
}

.info-list {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}

.info-item {
  margin-bottom: 4px;
}

.info-item .label {
  color: #909399;
}

.card-footer {
  text-align: center;
  margin-top: 8px;
}

.paid-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  color: #67c23a;
  font-size: 14px;
}

.pagination-wrap {
  margin-top: 20px;
  text-align: center;
}
</style>