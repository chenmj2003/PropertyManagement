<template>
  <div class="owner-parking">
    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="可购买车位" name="available">
        <div class="spot-grid">
          <el-card
            v-for="spot in availableSpots"
            :key="spot.id"
            shadow="hover"
            class="spot-card"
            :class="{ 'applying': spot.status === 'applying' }"
          >
            <div class="spot-header">
              <el-tag :type="spot.status === 'idle' ? 'success' : 'warning'" size="small">
                {{ spot.status === 'idle' ? '可购买' : '申请中' }}
              </el-tag>
            </div>
            <div class="spot-code">{{ spot.spotCode }}</div>
            <div class="spot-info">
              <p><strong>位置：</strong>{{ spot.location || '未指定' }}</p>
              <p><strong>售价：</strong><span class="price">¥{{ spot.price?.toFixed(2) }}</span></p>
            </div>
            <el-button
              type="primary"
              size="small"
              @click="applySpot(spot)"
              :loading="applyingId === spot.id"
              :disabled="spot.status === 'applying'"
              style="width: 100%; margin-top: 12px;"
            >
              {{ spot.status === 'applying' ? '已申请' : '立即申请' }}
            </el-button>
          </el-card>

          <el-empty v-if="availableSpots.length === 0 && !spotsLoading" description="暂无可购买车位" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的申请记录" name="myApplications">
        <el-table
          :data="pagedApps"
          border
          stripe
          v-loading="applicationsLoading"
        >
          <el-table-column prop="id" label="申请ID" width="80" />
          <el-table-column prop="spotId" label="车位ID" width="80" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag
                :type="statusType(row.status)"
                size="small"
              >
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rejectReason" label="拒绝原因" min-width="150">
            <template #default="{ row }">
              <span style="color: #f56c6c;">{{ row.rejectReason || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="170">
            <template #default="{ row }">{{ formatDate(row.applyTime) }}</template>
          </el-table-column>
          <el-table-column prop="approveTime" label="审核时间" width="170">
            <template #default="{ row }">{{ formatDate(row.approveTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'approved'"
                type="primary"
                size="small"
                @click="payForSpot(row)"
                :loading="payingId === row.id"
              >
                去支付
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="myApplications.length > appPageSize" v-model:current-page="appPage" :page-size="appPageSize" :total="myApplications.length" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end" />
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// ========== 状态 ==========
const activeTab = ref('available')
const availableSpots = ref([])
const myApplications = ref([])
const spotsLoading = ref(false)
const applicationsLoading = ref(false)
const appPage = ref(1); const appPageSize = ref(10)
const pagedApps = computed(() => myApplications.value.slice((appPage.value-1)*appPageSize.value, appPage.value*appPageSize.value))
const applyingId = ref(null)
const payingId = ref(null)

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

// ========== 数据加载 ==========
const loadAvailableSpots = async () => {
  spotsLoading.value = true
  try {
    const res = await axios.get('/api/owner/parking-spots', getHeaders())
    if (res.data.code === 200) {
      availableSpots.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '获取车位失败')
    }
  } catch (e) {
    ElMessage.error('获取车位列表失败')
  } finally {
    spotsLoading.value = false
  }
}

const loadMyApplications = async () => {
  applicationsLoading.value = true
  try {
    const res = await axios.get('/api/owner/parking-applications', getHeaders())
    if (res.data.code === 200) {
      myApplications.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '获取申请记录失败')
    }
  } catch (e) {
    ElMessage.error('获取申请记录失败')
  } finally {
    applicationsLoading.value = false
  }
}

// ========== 操作 ==========
const applySpot = async (spot) => {
  ElMessageBox.confirm(
    `确定要申请购买车位「${spot.spotCode}」吗？`,
    '确认申请',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    applyingId.value = spot.id
    try {
      const res = await axios.post(`/api/owner/parking-apply/${spot.id}`, {}, getHeaders())
      if (res.data.code === 200) {
        ElMessage.success('申请已提交，请等待管理员审核')
        loadAvailableSpots()
        loadMyApplications()
      } else {
        ElMessage.error(res.data.msg || '申请失败')
      }
    } catch (e) {
      ElMessage.error('申请失败')
    } finally {
      applyingId.value = null
    }
  }).catch(() => {})
}

const payForSpot = async (application) => {
  ElMessageBox.confirm(
    `确认支付车位购买申请${application.id}？`,
    '确认支付',
    {
      confirmButtonText: '去支付',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    payingId.value = application.id
    try {
      const res = await axios.post(`/api/owner/parking-pay/${application.id}`, {}, getHeaders())
      if (res.data.code === 200) {
        ElMessage.success('支付成功')
        loadMyApplications()
      } else {
        ElMessage.error(res.data.msg || '支付失败')
      }
    } catch (e) {
      ElMessage.error('支付失败')
    } finally {
      payingId.value = null
    }
  }).catch(() => {})
}

// ========== 工具方法 ==========
const statusType = (status) => {
  const map = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger',
    'paid': ''
  }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'paid': '已支付'
  }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ')
}

const handleTabChange = (tab) => {
  if (tab === 'available') {
    loadAvailableSpots()
  } else if (tab === 'myApplications') {
    loadMyApplications()
  }
}

onMounted(() => {
  loadAvailableSpots()
})
</script>

<style scoped>
.owner-parking {
  padding: 4px;
}

.spot-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
}

.spot-card {
  width: 280px;
  border-radius: 8px;
  transition: all 0.3s;
}

.spot-card:hover {
  transform: translateY(-2px);
}

.spot-card.applying {
  opacity: 0.6;
}

.spot-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.spot-code {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  margin: 8px 0;
}

.spot-info {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.original-price {
  color: #909399;
  text-decoration: line-through;
  font-size: 14px;
}

.flash-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}
</style>