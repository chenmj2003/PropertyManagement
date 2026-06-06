<template>
  <div class="owner-announcement">
    <h2 class="page-title">社区公告</h2>
    <div v-if="list.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无公告" />
    </div>
    <el-timeline v-loading="loading">
      <el-timeline-item
        v-for="item in list"
        :key="item.id"
        :timestamp="formatDate(item.createTime)"
        placement="top"
        color="#409eff"
      >
        <el-card shadow="hover" class="announce-card">
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-content">{{ item.content }}</p>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/owner/announcements', getHeaders())
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) { ElMessage.error('获取公告失败') }
  finally { loading.value = false }
}

const formatDate = (d) => { if (!d) return '-'; return d.replace('T', ' ') }

onMounted(() => loadData())
</script>

<style scoped>
.owner-announcement { padding: 4px; }
.page-title { margin: 0 0 20px 0; font-size: 20px; }
.empty-state { margin-top: 80px; }
.announce-card { border-radius: 8px; }
.card-title { margin: 0 0 12px 0; font-size: 16px; color: #303133; }
.card-content { margin: 0; font-size: 14px; color: #606266; line-height: 1.8; white-space: pre-wrap; }
</style>
