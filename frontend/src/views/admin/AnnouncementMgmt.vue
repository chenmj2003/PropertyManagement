<template>
  <div class="announcement-mgmt">
    <div class="header-bar">
      <h2>社区公告管理</h2>
      <el-button type="primary" @click="openAddDialog">发布公告</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发布时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="list.length === 0 && !loading" description="暂无公告" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="550px" :close-on-click-modal="false">
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
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
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = reactive({ title: '', content: '' })

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/announcements', getHeaders())
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) { ElMessage.error('获取公告失败') }
  finally { loading.value = false }
}

const openAddDialog = () => {
  isEdit.value = false; editId.value = null
  form.title = ''; form.content = ''
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true; editId.value = row.id
  form.title = row.title; form.content = row.content
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入内容'); return }
  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await axios.put(`/api/admin/announcements/${editId.value}`, { ...form }, getHeaders())
    } else {
      res = await axios.post('/api/admin/announcements', { ...form }, getHeaders())
    }
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
      dialogVisible.value = false
      loadData()
    } else { ElMessage.error(res.data.msg || '操作失败') }
  } catch (e) { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '警告', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.delete(`/api/admin/announcements/${row.id}`, getHeaders())
      if (res.data.code === 200) { ElMessage.success('已删除'); loadData() }
      else ElMessage.error(res.data.msg || '删除失败')
    } catch (e) { ElMessage.error('删除失败') }
  }).catch(() => {})
}

const formatDate = (d) => { if (!d) return '-'; return d.replace('T', ' ') }

onMounted(() => loadData())
</script>

<style scoped>
.announcement-mgmt { padding: 4px; }
.header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-bar h2 { margin: 0; font-size: 20px; }
</style>
