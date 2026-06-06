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
      <el-table-column prop="spotCode" label="车位编号" />
      <el-table-column prop="buildingId" label="楼栋ID" />
      <el-table-column prop="location" label="位置描述" />
      <el-table-column prop="price" label="售价" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createTime" label="创建时间" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const buildings = ref([])
const spots = ref([])
const publishing = ref(false)

const form = ref({
  buildingId: null,
  spotCode: '',
  location: '',
  price: undefined
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
    const res = await axios.get('/api/admin/parking/spots', getHeaders())
    spots.value = res.data.data
  } catch {
    ElMessage.error('获取车位列表失败')
  }
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

const clearForm = () => {
  form.value = {
    buildingId: null,
    spotCode: '',
    location: '',
    price: undefined
  }
}

onMounted(() => {
  fetchBuildings()
  fetchSpots()
})
</script>