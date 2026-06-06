<!--
============================================
文件: src/views/admin/ParkingSpotMgmt.vue
============================================
-->
<template>
  <div>
    <h2>车位发布</h2>
    <!-- 表单区域 -->
    <el-form
      :model="form"
      label-width="100px"
      @submit.prevent="onSubmit"
    >
      <el-form-item label="所属楼栋" required>
        <el-select v-model="form.buildingId" placeholder="选择楼栋">
          <el-option
            v-for="b in buildings"
            :key="b.id"
            :label="`${b.buildingNum}号楼 (${b.floorNum}层${b.roomNum}间)`"
            :value="b.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="车位编号" required>
        <el-input v-model="form.spotCode" placeholder="如 A-001" />
      </el-form-item>
      <el-form-item label="位置描述">
        <el-input v-model="form.location" placeholder="如 B1层东侧" />
      </el-form-item>
      <el-form-item label="售价">
        <el-input-number v-model="form.price" :min="0" style="width: 200px" />
      </el-form-item>
      <el-form-item label="车位图片">
        <div style="display:flex;align-items:center;gap:12px">
          <img v-if="form.image" :src="form.image" style="width:120px;height:80px;object-fit:cover;border-radius:8px" />
          <el-upload
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="uploadImage"
            accept="image/*"
          >
            <el-button size="small">上传图片</el-button>
          </el-upload>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="publishing">发布</el-button>
        <el-button @click="clearForm">重置</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <h2>车位列表</h2>
    <!-- 列表区域 -->
    <el-table :data="spots" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <img v-if="row.image" :src="row.image" style="width:60px;height:40px;object-fit:cover;border-radius:4px" />
          <span v-else style="color:#c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="spotCode" label="车位编号" />
      <el-table-column prop="buildingId" label="楼栋ID" />
      <el-table-column prop="location" label="位置描述" />
      <el-table-column prop="price" label="售价" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createTime" label="创建时间" />
    </el-table>
    <el-pagination
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      @current-change="fetchSpots"
      layout="total, prev, pager, next"
      style="margin-top:16px;justify-content:flex-end"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const buildings = ref([])
const spots = ref([])
const publishing = ref(false)
const currentPage = ref(1); const pageSize = ref(10); const total = ref(0)

const form = ref({
  buildingId: null,
  spotCode: '',
  location: '',
  price: undefined,
  image: ''
})

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const fetchBuildings = async () => {
  try {
    const res = await axios.get('/api/admin/buildings', getHeaders())
    buildings.value = res.data.data
  } catch {
    ElMessage.error('获取楼栋列表失败')
  }
}

const fetchSpots = async () => {
  try {
    const res = await axios.get('/api/admin/parking/spots', {
      ...getHeaders(),
      params: { page: currentPage.value, pageSize: pageSize.value }
    })
    if (res.data.code === 200) {
      spots.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch { ElMessage.error('获取车位列表失败') }
}

const onSubmit = async () => {
  if (!form.value.buildingId || !form.value.spotCode) {
    ElMessage.warning('请填写楼栋和车位编号')
    return
  }
  publishing.value = true
  try {
    await axios.post('/api/admin/parking/spots', form.value, getHeaders())
    ElMessage.success('发布成功')
    clearForm()
    fetchSpots()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '发布失败')
  } finally {
    publishing.value = false
  }
}

const beforeImageUpload = (file) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片不能超过5MB'); return false }
  return true
}

const uploadImage = async ({ file }) => {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const token = sessionStorage.getItem('token')
    const res = await axios.post('/api/upload', fd, {
      headers: { token }
    })
    if (res.data.code === 200) {
      form.value.image = res.data.data.url
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.data.msg || '上传失败')
    }
  } catch (e) { ElMessage.error('上传失败') }
}

const clearForm = () => {
  form.value = {
    buildingId: null,
    spotCode: '',
    location: '',
    price: undefined,
    image: ''
  }
}

onMounted(() => {
  fetchBuildings()
  fetchSpots()
})
</script>