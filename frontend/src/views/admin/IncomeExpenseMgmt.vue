<template>
  <div class="income-expense-mgmt">
    <h2 class="page-title">收支管理</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总收入</div>
          <div class="stat-value income">¥{{ stats.totalIncome?.toFixed(2) ?? '0.00' }}</div>
          <div class="stat-detail">
            物业费 ¥{{ stats.feeIncome?.toFixed(2) ?? '0.00' }} + 车位 ¥{{ stats.parkingIncome?.toFixed(2) ?? '0.00' }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总支出</div>
          <div class="stat-value expense">¥{{ stats.totalExpense?.toFixed(2) ?? '0.00' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">结余</div>
          <div class="stat-value" :class="(stats.balance ?? 0) >= 0 ? 'income' : 'expense'">
            ¥{{ stats.balance?.toFixed(2) ?? '0.00' }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">手动记账</div>
          <div class="stat-value" style="color: #409eff;">¥{{ stats.manualIncome?.toFixed(2) ?? '0.00' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <el-radio-group v-model="filter" size="small" @change="loadData">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="income">收入</el-radio-button>
        <el-radio-button label="expense">支出</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="openAddDialog">新增记录</el-button>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'income' ? 'success' : 'danger'" size="small">
              {{ row.type === 'income' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.type === 'income' ? '#52c41a' : '#f5222d' }">
              ¥{{ row.amount?.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="来源" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.id < 0" type="info" size="small">系统</el-tag>
            <el-tag v-else size="small">手动</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.id > 0" type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="row.id > 0" type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            <span v-if="row.id < 0" style="color: #c0c4cc;">系统记录</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="list.length === 0 && !loading" description="暂无收支记录" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑记录' : '新增记录'" width="500px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.type">
            <el-radio value="income">收入</el-radio>
            <el-radio value="expense">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <template v-if="form.type === 'income'">
              <el-option label="物业费" value="物业费" />
              <el-option label="车位费" value="车位费" />
              <el-option label="维修费" value="维修费" />
              <el-option label="其他收入" value="其他收入" />
            </template>
            <template v-else>
              <el-option label="维修支出" value="维修支出" />
              <el-option label="办公用品" value="办公用品" />
              <el-option label="水电费" value="水电费" />
              <el-option label="工资" value="工资" />
              <el-option label="其他支出" value="其他支出" />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="金额" required>
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.recordDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const stats = ref({})
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const filter = ref('')

const form = reactive({ type: 'expense', category: '', amount: 0, description: '', recordDate: '' })

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = filter.value ? { type: filter.value } : {}
    const res = await axios.get('/api/admin/income-expenses', { ...getHeaders(), params })
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) { ElMessage.error('获取数据失败') }
  finally { loading.value = false }
}

const loadStats = async () => {
  try {
    const res = await axios.get('/api/admin/income-expenses/stats', getHeaders())
    if (res.data.code === 200) stats.value = res.data.data || {}
  } catch (e) { /* ignore */ }
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.type = 'expense'
  form.category = ''
  form.amount = 0
  form.description = ''
  form.recordDate = ''
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.type = row.type
  form.category = row.category
  form.amount = row.amount
  form.description = row.description || ''
  form.recordDate = row.recordDate || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.type) { ElMessage.warning('请选择类型'); return }
  if (!form.category) { ElMessage.warning('请选择分类'); return }
  if (!form.amount || form.amount <= 0) { ElMessage.warning('请输入金额'); return }

  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await axios.put(`/api/admin/income-expenses/${editId.value}`, { ...form }, getHeaders())
    } else {
      res = await axios.post('/api/admin/income-expenses', { ...form }, getHeaders())
    }
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      loadData()
      loadStats()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (e) { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除这条${row.type === 'income' ? '收入' : '支出'}记录吗？`, '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/admin/income-expenses/${row.id}`, getHeaders())
      if (res.data.code === 200) {
        ElMessage.success('已删除')
        loadData()
        loadStats()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (e) { ElMessage.error('删除失败') }
  }).catch(() => {})
}

onMounted(() => { loadData(); loadStats() })
</script>

<style scoped>
.income-expense-mgmt { padding: 4px; }
.page-title { margin: 0 0 16px 0; font-size: 20px; }

.stat-card { text-align: center; }
.stat-label { font-size: 14px; color: #909399; }
.stat-value { font-size: 28px; font-weight: bold; margin-top: 8px; }
.stat-value.income { color: #52c41a; }
.stat-value.expense { color: #f5222d; }
.stat-detail { font-size: 12px; color: #909399; margin-top: 6px; }

.toolbar-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
