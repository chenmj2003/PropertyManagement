<template>
  <div class="parking-application-mgmt">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>车位购买申请审核</span>
        </div>
      </template>

      <el-table
        :data="applications"
        border
        stripe
        v-loading="loading"
      >
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="spotId" label="车位ID" width="80" />
        <el-table-column prop="ownerId" label="业主ID" width="80" />
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
        <el-table-column prop="applyTime" label="申请时间" width="170">
          <template #default="{ row }">{{ formatDate(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column prop="payAmount" label="支付金额" width="100">
          <template #default="{ row }">¥{{ row.payAmount?.toFixed(2) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="tradeNo" label="交易号" width="200">
          <template #default="{ row }">{{ row.tradeNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因" min-width="150">
          <template #default="{ row }">
            <span style="color: #f56c6c;">{{ row.rejectReason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div v-if="row.status === 'applying' || row.status === 'pending'">
              <el-button
                type="success"
                size="small"
                @click="approve(row)"
                :loading="operatingId === row.id && operateType === 'approve'"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="reject(row)"
                :loading="operatingId === row.id && operateType === 'reject'"
              >
                拒绝
              </el-button>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" @current-change="loadData" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const applications = ref([])
const loading = ref(false)
const operatingId = ref(null)
const operateType = ref('')
const currentPage = ref(1); const pageSize = ref(10); const total = ref(0)

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/parking/applications', {
      ...getHeaders(),
      params: { page: currentPage.value, pageSize: pageSize.value }
    })
    if (res.data.code === 200) {
      applications.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    } else { ElMessage.error(res.data.msg || '获取申请列表失败') }
  } catch (e) { ElMessage.error('获取申请列表失败') }
  finally {
    loading.value = false
  }
}

const approve = async (row) => {
  ElMessageBox.confirm(
    `确定要通过申请 ID:${row.id} 吗？`,
    '确认审核通过',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    operatingId.value = row.id
    operateType.value = 'approve'
    try {
      const res = await axios.put(`/api/admin/parking/approve/${row.id}`, {}, getHeaders())
      if (res.data.code === 200) {
        ElMessage.success('审核通过')
        loadData()
      } else {
        ElMessage.error(res.data.msg || '审核失败')
      }
    } catch (e) {
      ElMessage.error('审核失败')
    } finally {
      operatingId.value = null
      operateType.value = ''
    }
  }).catch(() => {})
}

const reject = async (row) => {
  ElMessageBox.prompt(
    '请输入拒绝原因',
    '审核拒绝',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入拒绝原因...'
    }
  ).then(async ({ value }) => {
    if (!value) {
      ElMessage.warning('请输入拒绝原因')
      return
    }
    operatingId.value = row.id
    operateType.value = 'reject'
    try {
      const res = await axios.put(
        `/api/admin/parking/reject/${row.id}`,
        { reason: value },
        getHeaders()
      )
      if (res.data.code === 200) {
        ElMessage.success('已拒绝该申请')
        loadData()
      } else {
        ElMessage.error(res.data.msg || '操作失败')
      }
    } catch (e) {
      ElMessage.error('操作失败')
    } finally {
      operatingId.value = null
      operateType.value = ''
    }
  }).catch(() => {})
}

const statusType = (status) => {
  const map = {
    'applying': 'warning',
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger',
    'paid': ''
  }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = {
    'applying': '待审核', 
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.parking-application-mgmt {
  max-width: 1400px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>