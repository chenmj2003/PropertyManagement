<template>
  <div class="repair-mgmt">
    <div class="header-bar">
      <h2>报修管理</h2>
      <el-radio-group v-model="filter" size="small" @change="loadData">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="pending">待处理</el-radio-button>
        <el-radio-button label="processing">处理中</el-radio-button>
        <el-radio-button label="completed">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <el-card shadow="never">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="ownerName" label="报修人" width="100" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="title" label="报修标题" min-width="140" />
        <el-table-column prop="description" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" type="warning" size="small" @click="handleProcess(row)">开始处理</el-button>
            <el-button v-if="row.status === 'processing'" type="success" size="small" @click="handleComplete(row)">标记完成</el-button>
            <span v-if="row.status === 'completed'" style="color: #909399;">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" @current-change="loadData" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end" />
      <el-empty v-if="list.length === 0 && !loading" description="暂无报修记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const filter = ref('all')
const currentPage = ref(1); const pageSize = ref(10); const total = ref(0)

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, pageSize: pageSize.value }
    if (filter.value !== 'all') params.status = filter.value
    const res = await axios.get('/api/admin/repairs', { ...getHeaders(), params })
    if (res.data.code === 200) {
      list.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch (e) { ElMessage.error('获取报修列表失败') }
  finally { loading.value = false }
}

const handleProcess = (row) => {
  ElMessageBox.confirm(`确定开始处理「${row.title}」吗？`, '确认', { type: 'info' }).then(async () => {
    try {
      const res = await axios.put(`/api/admin/repairs/${row.id}/process`, {}, getHeaders())
      if (res.data.code === 200) { ElMessage.success('已标记为处理中'); loadData() }
      else ElMessage.error(res.data.msg || '操作失败')
    } catch (e) { ElMessage.error('操作失败') }
  }).catch(() => {})
}

const handleComplete = (row) => {
  ElMessageBox.confirm(`确定将「${row.title}」标记为已完成吗？`, '确认', { type: 'info' }).then(async () => {
    try {
      const res = await axios.put(`/api/admin/repairs/${row.id}/complete`, {}, getHeaders())
      if (res.data.code === 200) { ElMessage.success('已标记为已完成'); loadData() }
      else ElMessage.error(res.data.msg || '操作失败')
    } catch (e) { ElMessage.error('操作失败') }
  }).catch(() => {})
}

const statusType = (s) => ({ 'pending': 'danger', 'processing': 'warning', 'completed': 'success' }[s] || 'info')
const statusText = (s) => ({ 'pending': '待处理', 'processing': '处理中', 'completed': '已完成' }[s] || s)

const formatDate = (d) => {
  if (!d) return '-'
  return d.replace('T', ' ')
}

onMounted(() => loadData())
</script>

<style scoped>
.repair-mgmt { padding: 4px; }
.header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-bar h2 { margin: 0; font-size: 20px; }
</style>
