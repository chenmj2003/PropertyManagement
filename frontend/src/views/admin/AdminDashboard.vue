<template>
  <div class="dashboard">
    <h2 class="page-title">首页概览</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6f7ff;">
            <el-icon :size="28" color="#1890ff"><User /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ data.ownerCount ?? 0 }}</div>
            <div class="stat-label">业主总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f6ffed;">
            <el-icon :size="28" color="#52c41a"><Van /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ data.spotTotal ?? 0 }}</div>
            <div class="stat-label">车位总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #fff7e6;">
            <el-icon :size="28" color="#fa8c16"><Clock /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ data.pendingAppCount ?? 0 }}</div>
            <div class="stat-label">待审核申请</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #fff1f0;">
            <el-icon :size="28" color="#f5222d"><Bell /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ data.unpaidNoticeCount ?? 0 }}</div>
            <div class="stat-label">待缴费通知</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：车位和缴费详细统计 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>车位统计</span></template>
          <el-row :gutter="12">
            <el-col :span="8">
              <div class="mini-stat">
                <div class="mini-value green">{{ data.spotIdle ?? 0 }}</div>
                <div class="mini-label">空闲</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="mini-stat">
                <div class="mini-value orange">{{ data.spotApplying ?? 0 }}</div>
                <div class="mini-label">申购中</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="mini-stat">
                <div class="mini-value blue">{{ data.spotSold ?? 0 }}</div>
                <div class="mini-label">已售</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>缴费通知统计</span></template>
          <el-row :gutter="12">
            <el-col :span="12">
              <div class="mini-stat">
                <div class="mini-value red">{{ data.unpaidNoticeCount ?? 0 }}</div>
                <div class="mini-label">待缴费</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="mini-stat">
                <div class="mini-value green">{{ data.paidNoticeCount ?? 0 }}</div>
                <div class="mini-label">已缴费</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 申请统计 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>车位申请统计</span></template>
          <el-row :gutter="12">
            <el-col :span="8">
              <div class="mini-stat">
                <div class="mini-value orange">{{ data.pendingAppCount ?? 0 }}</div>
                <div class="mini-label">待审核</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="mini-stat">
                <div class="mini-value blue">{{ data.approvedAppCount ?? 0 }}</div>
                <div class="mini-label">待支付</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="mini-stat">
                <div class="mini-value green">{{ data.paidAppCount ?? 0 }}</div>
                <div class="mini-label">已完成</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 最近申请 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>最近申请记录</span></template>
          <el-table :data="data.recentApps ?? []" size="small" max-height="220">
            <el-table-column prop="id" label="ID" width="50" />
            <el-table-column prop="spotId" label="车位ID" width="70" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyTime" label="申请时间" width="160">
              <template #default="{ row }">{{ formatDate(row.applyTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { User, Van, Clock, Bell } from '@element-plus/icons-vue'

const data = ref({})

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadDashboard = async () => {
  try {
    const res = await axios.get('/api/admin/dashboard', getHeaders())
    if (res.data.code === 200) {
      data.value = res.data.data
    }
  } catch (e) {
    // ignore
  }
}

const statusType = (status) => {
  const map = { 'applying': 'warning', 'approved': 'success', 'rejected': 'danger', 'paid': '' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { 'applying': '待审核', 'approved': '已通过', 'rejected': '已拒绝', 'paid': '已支付' }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ')
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.dashboard {
  padding: 4px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.mini-stat {
  text-align: center;
  padding: 12px 0;
}

.mini-value {
  font-size: 32px;
  font-weight: bold;
}

.mini-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.mini-value.green { color: #52c41a; }
.mini-value.orange { color: #fa8c16; }
.mini-value.blue { color: #1890ff; }
.mini-value.red { color: #f5222d; }
</style>
