<!--
============================================
文件: src/views/admin/OwnerList.vue
============================================
-->
<template>
  <div class="owner-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>业主信息管理</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchName"
          placeholder="请输入业主姓名"
          style="width: 200px"
          clearable
          @clear="loadData"
        />
        <el-button type="primary" @click="loadData" style="margin-left: 10px">
          <el-icon><Search /></el-icon>查询
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="pagedData" border stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="buildingName" label="楼栋" width="100" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />
      </el-table>
      <el-pagination v-if="filteredData.length > pageSize" v-model:current-page="currentPage" :page-size="pageSize" :total="filteredData.length" layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
// computed needed for pagedData + search filter
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const tableData = ref([]); const filteredData = ref([])
const searchName = ref('')
const currentPage = ref(1); const pageSize = ref(10)
const pagedData = computed(() => filteredData.value.slice((currentPage.value-1)*pageSize.value, currentPage.value*pageSize.value))

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

const loadData = async () => {
  try {
    const res = await axios.get('/api/admin/owners', getHeaders())
    if (res.data.code === 200) {
      let data = res.data.data
      if (searchName.value) data = data.filter(item => item.name.includes(searchName.value))
      tableData.value = data; filteredData.value = data; currentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('获取业主列表失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.owner-list {
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