<!--
============================================
文件: src/views/admin/VehicleMgmt.vue
============================================
-->
<template>
  <div class="vehicle-mgmt">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>车辆信息管理</span>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="车牌号 / 品牌 / 业主姓名"
          style="width: 280px"
          clearable
          @keyup.enter="loadData"
        />
        <el-button type="primary" @click="loadData" style="margin-left: 10px">
          <el-icon><Search /></el-icon>查询
        </el-button>
      </div>

      <el-table
        :data="pagedData"
        border
        stripe
        v-loading="loading"
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="color" label="颜色" width="80" />
        <el-table-column prop="vehicleType" label="类型" width="80" />
        <el-table-column prop="ownerName" label="业主姓名" width="100" />
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column prop="createTime" label="登记时间" width="170">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.substring(0, 16).replace('T', ' ') : '-' }}
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="filteredData.length > pageSize" v-model:current-page="currentPage" :page-size="pageSize" :total="filteredData.length" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
// computed for client-side pagination
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const tableData = ref([]); const filteredData = ref([])
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(1); const pageSize = ref(10)
const pagedData = computed(() => filteredData.value.slice((currentPage.value-1)*pageSize.value, currentPage.value*pageSize.value))

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/vehicles', {
      ...getHeaders(),
      params: { keyword: keyword.value }
    })
    if (res.data.code === 200) {
      tableData.value = res.data.data; filteredData.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取车辆列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.vehicle-mgmt {
  max-width: 1200px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-bar {
  display: flex;
  align-items: center;
}
</style>