<template>
  <div class="owner-layout">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-title">业主中心</div>
      <div
        class="sidebar-item"
        :class="{ active: currentMenu === 'info' }"
        @click="currentMenu = 'info'"
      >
        <el-icon><User /></el-icon>
        <span>个人信息</span>
      </div>
      <div
        class="sidebar-item"
        :class="{ active: currentMenu === 'vehicle' }"
        @click="currentMenu = 'vehicle'"
      >
        <el-icon><Van /></el-icon>
        <span>我的车辆</span>
      </div>
      <div
        class="sidebar-item"
        :class="{ active: currentMenu === 'announcement' }"
        @click="currentMenu = 'announcement'"
      >
        <el-icon><Bell /></el-icon>
        <span>社区公告</span>
      </div>
      <div
        class="sidebar-item"
        :class="{ active: currentMenu === 'payment' }"
        @click="currentMenu = 'payment'"
      >
        <el-icon><Money /></el-icon>
        <span>缴费通知</span>
      </div>
      <div
        class="sidebar-item"
        :class="{ active: currentMenu === 'repair' }"
        @click="currentMenu = 'repair'"
      >
        <el-icon><Tools /></el-icon>
        <span>报修申请</span>
      </div>
      <div
        class="sidebar-item"
        :class="{ active: currentMenu === 'parking' }"
        @click="currentMenu = 'parking'"
      >
        <el-icon><Shop /></el-icon>
        <span>车位购买</span>
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="main-content">
      <!-- 全局顶栏 -->
      <div class="top-header">
        <span class="header-greeting">欢迎回来，{{ formData.name || '业主' }}</span>
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </div>

      <!-- 个人信息页 -->
      <div v-if="currentMenu === 'info'" class="page-container">
        <div class="header-bar">
          <h2>个人信息</h2>
        </div>

        <el-card class="info-card" shadow="never">
          <template v-if="!isEditing">
            <el-form :model="formData" label-width="80px">
              <el-form-item label="姓名">
                <span>{{ formData.name }}</span>
              </el-form-item>
              <el-form-item label="账号">
                <span>{{ formData.account }}</span>
              </el-form-item>
              <el-form-item label="性别">
                <span>{{ formData.gender }}</span>
              </el-form-item>
              <el-form-item label="年龄">
                <span>{{ formData.age }}</span>
              </el-form-item>
              <el-form-item label="手机号">
                <span>{{ formData.phone }}</span>
              </el-form-item>
              <el-form-item label="楼栋">
                <span>{{ formData.buildingId || '-' }}</span>
              </el-form-item>
              <el-form-item label="房号">
                <span>{{ formData.roomNumber || '-' }}</span>
              </el-form-item>
            </el-form>
            <div style="text-align: center; margin-top: 20px;">
              <el-button type="primary" @click="enterEdit">修改信息</el-button>
            </div>
          </template>

          <template v-else>
            <el-form :model="formData" label-width="80px" :rules="rules" ref="formRef">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="formData.name" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="账号" prop="account">
                <el-input v-model="formData.account" placeholder="请输入账号" />
              </el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-select v-model="formData.gender" placeholder="请选择性别" style="width: 100%;">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
              </el-form-item>
              <el-form-item label="年龄" prop="age">
                <el-input-number v-model="formData.age" :min="1" :max="150" style="width: 100%;" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="formData.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="楼栋">
                <span>{{ formData.buildingId || '-' }}</span>
              </el-form-item>
              <el-form-item label="房号">
                <span>{{ formData.roomNumber || '-' }}</span>
              </el-form-item>
            </el-form>
            <div style="text-align: center; margin-top: 20px;">
              <el-button @click="cancelEdit">取消</el-button>
              <el-button type="primary" @click="saveInfo">保存</el-button>
            </div>
          </template>
        </el-card>
      </div>

      <!-- 社区公告页 -->
      <div v-if="currentMenu === 'announcement'" class="page-container">
        <OwnerAnnouncement />
      </div>
      <!-- 缴费通知页 -->
      <div v-if="currentMenu === 'payment'" class="page-container">
        <OwnerPayment />
      </div>
      <div v-if="currentMenu === 'parking'" class="page-container">
        <OwnerParking />
      </div>
      <!-- 报修申请页 -->
      <div v-if="currentMenu === 'repair'" class="page-container">
        <OwnerRepair />
      </div>
      <!-- 车辆管理页 -->
      <div v-if="currentMenu === 'vehicle'" class="page-container">
        <div class="header-bar">
          <h2>我的车辆</h2>
          <el-button type="primary" size="small" @click="openAddDialog">添加车辆</el-button>
        </div>

        <el-card shadow="never">
          <el-table :data="vehicleList" border stripe v-loading="vehicleLoading">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="plateNumber" label="车牌号" width="120" />
            <el-table-column prop="brand" label="品牌" width="100" />
            <el-table-column prop="model" label="型号" width="120" />
            <el-table-column prop="color" label="颜色" width="80" />
            <el-table-column prop="vehicleType" label="类型" width="80" />
            <el-table-column prop="remark" label="备注" min-width="150" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
                <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 添加/编辑弹窗 -->
        <el-dialog
          :title="dialogTitle"
          v-model="dialogVisible"
          width="500px"
          :close-on-click-modal="false"
        >
          <el-form :model="vehicleForm" label-width="80px" :rules="vehicleRules" ref="vehicleFormRef">
            <el-form-item label="车牌号" prop="plateNumber">
              <el-input v-model="vehicleForm.plateNumber" placeholder="请输入车牌号" />
            </el-form-item>
            <el-form-item label="品牌">
              <el-input v-model="vehicleForm.brand" placeholder="如：丰田" />
            </el-form-item>
            <el-form-item label="型号">
              <el-input v-model="vehicleForm.model" placeholder="如：卡罗拉" />
            </el-form-item>
            <el-form-item label="颜色">
              <el-input v-model="vehicleForm.color" placeholder="如：白色" />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="vehicleForm.vehicleType" placeholder="请选择类型" style="width: 100%;">
                <el-option label="轿车" value="轿车" />
                <el-option label="SUV" value="SUV" />
                <el-option label="MPV" value="MPV" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                v-model="vehicleForm.remark"
                type="textarea"
                :rows="3"
                placeholder="备注信息"
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitVehicle" :loading="submitting">确定</el-button>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { User, Van, Money, Shop, Tools, Bell } from '@element-plus/icons-vue'
import OwnerParking from './OwnerParking.vue'
import OwnerPayment from './OwnerPayment.vue'
import OwnerRepair from './OwnerRepair.vue'
import OwnerAnnouncement from './OwnerAnnouncement.vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const currentMenu = ref('info')
const isEditing = ref(false)

// ========== 个人信息相关 ==========
const formData = reactive({
  name: '',
  account: '',
  gender: '',
  age: 0,
  phone: '',
  buildingId: '',
  roomNumber: ''
})
const originalData = reactive({})
const formRef = ref(null)

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// ========== 车辆相关 ==========
const vehicleList = ref([])
const vehicleLoading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const vehicleFormRef = ref(null)

const vehicleForm = reactive({
  plateNumber: '',
  brand: '',
  model: '',
  color: '',
  vehicleType: '',
  remark: ''
})

const vehicleRules = {
  plateNumber: [{ required: true, message: '请输入车牌号', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑车辆' : '添加车辆')

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
}

// ========== 个人信息方法 ==========
const loadInfo = async () => {
  try {
    const res = await axios.get('/api/owner/info', getHeaders())
    if (res.data.code === 200) {
      const data = res.data.data
      Object.assign(formData, data)
      Object.assign(originalData, data)
    }
  } catch (e) {
    ElMessage.error('获取个人信息失败')
  }
}

const enterEdit = () => {
  Object.assign(originalData, formData)
  isEditing.value = true
}

const cancelEdit = () => {
  Object.assign(formData, originalData)
  isEditing.value = false
}

const saveInfo = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    const res = await axios.put('/api/owner/info', {
      name: formData.name,
      account: formData.account,
      gender: formData.gender,
      age: formData.age,
      phone: formData.phone
    }, getHeaders())

    if (res.data.code === 200) {
      ElMessage.success('修改成功')
      Object.assign(originalData, formData)
      isEditing.value = false
    } else {
      ElMessage.error(res.data.msg || '修改失败')
    }
  } catch (e) {
    ElMessage.error('修改失败')
  }
}

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const token = sessionStorage.getItem('token')
      if (token) {
        await axios.post('/api/owner/logout', {}, { headers: { token } })
      }
    } catch (e) {}
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('token')
    router.push('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}

// ========== 车辆方法 ==========
const loadVehicles = async () => {
  vehicleLoading.value = true
  try {
    const res = await axios.get('/api/owner/vehicles', getHeaders())
    if (res.data.code === 200) {
      vehicleList.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('获取车辆列表失败')
  } finally {
    vehicleLoading.value = false
  }
}

const resetVehicleForm = () => {
  vehicleForm.plateNumber = ''
  vehicleForm.brand = ''
  vehicleForm.model = ''
  vehicleForm.color = ''
  vehicleForm.vehicleType = ''
  vehicleForm.remark = ''
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  resetVehicleForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  vehicleForm.plateNumber = row.plateNumber
  vehicleForm.brand = row.brand || ''
  vehicleForm.model = row.model || ''
  vehicleForm.color = row.color || ''
  vehicleForm.vehicleType = row.vehicleType || ''
  vehicleForm.remark = row.remark || ''
  dialogVisible.value = true
}

const submitVehicle = async () => {
  const valid = await vehicleFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await axios.put(`/api/owner/vehicle/${editId.value}`, {
        plateNumber: vehicleForm.plateNumber,
        brand: vehicleForm.brand,
        model: vehicleForm.model,
        color: vehicleForm.color,
        vehicleType: vehicleForm.vehicleType,
        remark: vehicleForm.remark
      }, getHeaders())
    } else {
      res = await axios.post('/api/owner/vehicle', {
        plateNumber: vehicleForm.plateNumber,
        brand: vehicleForm.brand,
        model: vehicleForm.model,
        color: vehicleForm.color,
        vehicleType: vehicleForm.vehicleType,
        remark: vehicleForm.remark
      }, getHeaders())
    }

    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      loadVehicles()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除车辆「${row.plateNumber}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/owner/vehicle/${row.id}`, getHeaders())
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadVehicles()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadInfo()
  loadVehicles()
})
</script>

<style scoped>
.owner-layout {
  display: flex;
  height: 100vh;
  background-image: url('@/assets/images/ownerbg.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
}

.sidebar {
  width: 220px;
  background-color: rgba(48, 65, 86, 0.92);
  color: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-title {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  cursor: pointer;
  color: #bfcbd9;
  font-size: 15px;
  transition: all 0.2s;
}

.sidebar-item:hover {
  background-color: #263445;
  color: #fff;
}

.sidebar-item.active {
  background-color: #409eff;
  color: #fff;
}

.main-content {
  flex: 1;
  background-color: rgba(240, 242, 245, 0.6);
  overflow-y: auto;
}

.top-header {
  height: 50px;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
  border-bottom: 1px solid #e4e7ed;
  gap: 16px;
}

.header-greeting {
  font-size: 14px;
  color: #606266;
}

.page-container {
  padding: 24px;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-bar h2 {
  margin: 0;
  font-size: 20px;
}

.info-card {
  max-width: 500px;
  margin: 0 auto;
}
</style>