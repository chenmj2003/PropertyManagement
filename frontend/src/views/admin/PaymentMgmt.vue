<template>
  <div class="payment-mgmt">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="业主姓名">
          <el-input v-model="searchForm.ownerName" placeholder="输入业主姓名" clearable />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="searchForm.roomNumber" placeholder="输入房间号" clearable />
        </el-form-item>
        <el-form-item label="费项类型">
          <el-select v-model="searchForm.feeType" placeholder="全部" clearable>
            <el-option label="物业费" value="property" />
            <el-option label="水费" value="water" />
            <el-option label="电费" value="electric" />
          </el-select>
        </el-form-item>
        <el-form-item label="缴费状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待缴费" value="unpaid" />
            <el-option label="已缴费" value="paid" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button type="success" @click="showSendDialog">
            <el-icon><Plus /></el-icon>
            发送缴费通知
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="pagedList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="ownerName" label="业主" width="100" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="feeTypeName" label="费项" width="100" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'paid' ? 'success' : 'danger'" size="small">
              {{ row.status === 'paid' ? '已缴费' : '待缴费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期" width="170">
          <template #default="{ row }">{{ formatDate(row.deadline) }}</template>
        </el-table-column>
        <el-table-column prop="payTime" label="缴费时间" width="170">
          <template #default="{ row }">{{ formatDate(row.payTime) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>
      <el-pagination v-if="list.length > pageSize" v-model:current-page="currentPage" :page-size="pageSize" :total="list.length" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <!-- 发送通知弹窗 -->
    <el-dialog v-model="dialogVisible" title="发送缴费通知" width="550px">
      <el-form :model="sendForm" label-width="100px">
        <el-form-item label="选择业主">
          <el-select
            v-model="sendForm.ownerIds"
            multiple
            filterable
            placeholder="请选择业主（可多选）"
            style="width: 100%"
          >
            <el-option
              v-for="owner in ownerList"
              :key="owner.id"
              :label="owner.name + ' - ' + owner.roomNumber"
              :value="owner.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="缴费类型">
          <el-select v-model="sendForm.feeType" placeholder="请选择" style="width: 100%">
            <el-option label="物业费" value="property" />
            <el-option label="水费" value="water" />
            <el-option label="电费" value="electric" />
          </el-select>
        </el-form-item>
        <el-form-item label="缴费金额">
          <el-input-number v-model="sendForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="sendForm.deadline"
            type="datetime"
            placeholder="选择截止日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="sendForm.description" type="textarea" :rows="2" placeholder="如：2025年第一季度物业费" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSend" :loading="sending">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
// computed for client-side pagination
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

const list = ref([]); const allData = ref([])
const currentPage = ref(1); const pageSize = ref(10)
const pagedList = computed(() => allData.value.slice((currentPage.value-1)*pageSize.value, currentPage.value*pageSize.value))
const ownerList = ref([])
const loading = ref(false)
const sending = ref(false)
const dialogVisible = ref(false)

const searchForm = reactive({ ownerName: '', roomNumber: '', feeType: '', status: '' })
const sendForm = reactive({
  ownerIds: [],
  feeType: '',
  amount: 0,
  description: '',
  deadline: ''
})

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.ownerName) params.ownerName = searchForm.ownerName
    if (searchForm.roomNumber) params.roomNumber = searchForm.roomNumber
    if (searchForm.feeType) params.feeType = searchForm.feeType
    if (searchForm.status) params.status = searchForm.status

    const res = await axios.get('/api/admin/paymentNotifications', { ...getHeaders(), params })
    if (res.data.code === 200) {
      list.value = res.data.data || []; allData.value = list.value; currentPage.value = 1
    } else {
      ElMessage.error(res.data.msg || '查询失败')
    }
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const loadOwners = async () => {
  try {
    const res = await axios.get('/api/admin/owners', getHeaders())
    if (res.data.code === 200) {
      ownerList.value = res.data.data || []
    }
  } catch (e) {
    // ignore
  }
}

const showSendDialog = () => {
  sendForm.ownerIds = []
  sendForm.feeType = ''
  sendForm.amount = 0
  sendForm.description = ''
  sendForm.deadline = ''
  dialogVisible.value = true
}

const handleSend = async () => {
  if (sendForm.ownerIds.length === 0) {
    ElMessage.warning('请至少选择一位业主')
    return
  }
  if (!sendForm.feeType) {
    ElMessage.warning('请选择缴费类型')
    return
  }
  if (!sendForm.amount || sendForm.amount <= 0) {
    ElMessage.warning('请输入正确的缴费金额')
    return
  }

  sending.value = true
  try {
    const res = await axios.post('/api/admin/sendNotification', {
      ownerIds: sendForm.ownerIds,
      feeType: sendForm.feeType,
      amount: sendForm.amount,
      description: sendForm.description,
      deadline: sendForm.deadline
    }, getHeaders())

    if (res.data.code === 200) {
      ElMessage.success('缴费通知发送成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.msg || '发送失败')
    }
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ')
}

onMounted(() => {
  loadData()
  loadOwners()
})
</script>

<style scoped>
.payment-mgmt {
  padding: 4px;
}
.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}
</style>
