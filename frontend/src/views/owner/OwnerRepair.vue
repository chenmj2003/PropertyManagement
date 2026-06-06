<template>
  <div class="owner-repair">
    <div class="header-bar">
      <h2>报修申请</h2>
      <el-button type="primary" @click="openAddDialog">提交报修</el-button>
    </div>

    <!-- 报修列表 -->
    <el-card shadow="never">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="报修标题" min-width="150" />
        <el-table-column prop="description" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="170">
          <template #default="{ row }">{{ formatDate(row.completeTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="list.length === 0 && !loading" description="暂无报修记录" />
    </el-card>

    <!-- 提交报修弹窗 -->
    <el-dialog v-model="dialogVisible" title="提交报修" width="500px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="报修标题" required>
          <el-input v-model="form.title" placeholder="如：厨房水管漏水" />
        </el-form-item>
        <el-form-item label="故障描述" required>
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述故障情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)

const form = reactive({ title: '', description: '' })

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/owner/repairs', getHeaders())
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) {
    ElMessage.error('获取报修列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  form.title = ''
  form.description = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title.trim()) { ElMessage.warning('请填写报修标题'); return }
  if (!form.description.trim()) { ElMessage.warning('请填写故障描述'); return }

  submitting.value = true
  try {
    const res = await axios.post('/api/owner/repairs', {
      title: form.title,
      description: form.description
    }, getHeaders())
    if (res.data.code === 200) {
      ElMessage.success('报修提交成功')
      dialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const statusType = (s) => ({ 'pending': 'danger', 'processing': 'warning', 'completed': 'success' }[s] || 'info')
const statusText = (s) => ({ 'pending': '待处理', 'processing': '处理中', 'completed': '已完成' }[s] || s)

const formatDate = (d) => {
  if (!d) return '-'
  return d.replace('T', ' ')
}

onMounted(() => loadList())
</script>

<style scoped>
.owner-repair { padding: 4px; }
.header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-bar h2 { margin: 0; font-size: 20px; }
</style>
