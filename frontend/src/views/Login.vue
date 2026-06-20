<template>
  <div class="login-container" :style="{ backgroundImage: 'url(' + bgImg + ')' }">
    <div class="login-card">
      <!-- 顶部标题区 -->
      <div class="card-header">
        <div class="logo-icon">
          <el-icon :size="36"><OfficeBuilding /></el-icon>
        </div>
        <h1 class="app-title">智慧物业管理系统</h1>
        <p class="app-subtitle" v-if="mode === 'login'">欢迎回来，请登录您的账户</p>
        <p class="app-subtitle" v-else-if="mode === 'register'">创建新账户，加入社区</p>
        <p class="app-subtitle" v-else>重置您的登录密码</p>
      </div>

      <!-- 登录模式切换 -->
      <div class="tabs" v-if="mode === 'login'">
        <el-radio-group v-model="userType" size="large">
          <el-radio-button label="owner">业主</el-radio-button>
          <el-radio-button label="admin">物业人员</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 登录表单 -->
      <el-form v-if="mode === 'login'" :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="account">
          <el-input v-model="loginForm.account" placeholder="请输入账号" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loginLoading" size="large" class="login-btn">登 录</el-button>
        </el-form-item>
        <div class="links">
          <span @click="mode = 'register'">注册业主</span>
          <span @click="mode = 'reset'">忘记密码</span>
        </div>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-if="mode === 'register'" :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="80px" @keyup.enter="handleRegister">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" />
        </el-form-item>
        <el-form-item label="账号" prop="account">
          <el-input v-model="registerForm.account" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="registerForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="registerForm.age" />
        </el-form-item>
          <!-- 楼栋选择 -->
          <el-form-item label="楼栋" prop="buildingId">
            <el-select v-model="registerForm.buildingId" placeholder="请选择楼栋">
            <el-option v-for="b in buildingList" :key="b.id" :label="b.buildingNum + '号楼'" :value="b.id" />
            </el-select>
          </el-form-item>

<!-- 房间号（可输入，后端会校验） -->
          <el-form-item label="房间号" prop="roomNumber">
            <el-input v-model="registerForm.roomNumber" placeholder="例如 1-101" />
            <!-- 或者用下拉选择，但需要后端返回房间列表，此处先用输入框，后端会校验合法性 -->
          </el-form-item>
        <el-form-item class="btn-row">
          <el-button type="primary" @click="handleRegister" :loading="regLoading" class="action-btn">注 册</el-button>
          <el-button @click="mode = 'login'" class="back-btn">返回登录</el-button>
        </el-form-item>
      </el-form>

      <!-- 忘记密码表单 -->
      <el-form v-if="mode === 'reset'" :model="resetForm" :rules="resetRules" ref="resetFormRef" label-width="80px" @keyup.enter="handleReset">
        <el-form-item label="账号" prop="account">
          <el-input v-model="resetForm.account" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="resetForm.phone" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmNew">
          <el-input v-model="resetForm.confirmNew" type="password" show-password />
        </el-form-item>
        <el-form-item class="btn-row">
          <el-button type="primary" @click="handleReset" :loading="resetLoading" class="action-btn">重 置</el-button>
          <el-button @click="mode = 'login'" class="back-btn">返回登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { OfficeBuilding } from '@element-plus/icons-vue'
import { onMounted } from 'vue'
// 导入背景图
import bgImg from '@/assets/images/bg.jpg'

const router = useRouter()
const userType = ref('owner')
const mode = ref('login') // login, register, reset

// ============ 登录逻辑 ============
const loginForm = reactive({ account: '', password: '' })
const loginFormRef = ref(null)
const loginLoading = ref(false)
const loginRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}



const fetchBuildings = async () => {
  try {
    const { data } = await axios.get('/api/buildings')  // 需要后端提供这个接口（见下）
    if (data.code === 200) {
      buildingList.value = data.data
    }
  } catch (e) {
    console.error('获取楼栋列表失败')
  }
}
onMounted(() => {
  fetchBuildings()
})

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  loginLoading.value = true
  try {
    const url = userType.value === 'admin' ? '/api/adminLogin' : '/api/ownerLogin'
    const { data } = await axios.post(url, loginForm)
    
    if (data.code === 200) {
      // ✅ 修复 1：根据你的后端实际返回结构调整
      const token = data.data?.token || data.token  // 兼容两种格式
      const role = data.data?.role || data.role      // 兼容两种格式
      
      if (token) {
  // 1. 存储到 sessionStorage
  sessionStorage.setItem('token', token)
  sessionStorage.setItem('role', role || userType.value)
  
  // 2. 🔥 同时存储到 cookie（关键！支付宝跳转后可能 sessionStorage 丢失）
  const expires = new Date()
  expires.setDate(expires.getDate() + 1) // 1天后过期
  document.cookie = `token=${token}; path=/; expires=${expires.toUTCString()}; SameSite=Lax`
  document.cookie = `role=${role || userType.value}; path=/; expires=${expires.toUTCString()}; SameSite=Lax`
  
  console.log('✅ Token 已保存到 sessionStorage 和 cookie')
}
      
      ElMessage.success(data.message || data.msg || '登录成功')
      
      // ✅ 修复 2：使用具体路径跳转
      if (userType.value === 'admin') {
        router.push('/admin/dashboard')  // 默认进入仪表盘首页
      } else {
        router.push('/owner/home')     // 直接跳转到具体页面
      }
    } else {
      ElMessage.error(data.message || data.msg)
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error('网络错误')
  } finally {
    loginLoading.value = false
  }
}

// ============ 注册逻辑 ============
const registerForm = reactive({
  name: '',
  account: '',
  password: '',
  confirmPassword: '',
  phone: '',
  gender: '男',
  age: undefined,
  buildingId: undefined,   // 新增
  roomNumber: ''           // 新增
})

const buildingList = ref([])   // 保存从后端获取的楼栋列表
const registerFormRef = ref(null)
const regLoading = ref(false)
const validatePhone = (rule, value, callback) => {
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号错误，请重新输入'))
  } else {
    callback()
  }
}
const validatePass = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}
const registerRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass, trigger: 'blur' }],
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }]
}

const handleRegister = async () => {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  regLoading.value = true
  try {
    const { data } = await axios.post('/api/register', registerForm)
    if (data.code === 200) {
      ElMessage.success(data.message || data.msg)
      mode.value = 'login'
      // 清空表单
      Object.keys(registerForm).forEach(key => {
  registerForm[key] = undefined
})
  registerForm.gender = '男'
  registerForm.buildingId = undefined
  registerForm.roomNumber = ''
    } else {
      ElMessage.error(data.message || data.msg)
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    regLoading.value = false
  }
}

// ============ 重置密码逻辑 ============
const resetForm = reactive({
  account: '',
  phone: '',
  newPassword: '',
  confirmNew: ''
})
const resetFormRef = ref(null)
const resetLoading = ref(false)
const validateResetPass = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}
const resetRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmNew: [{ required: true, validator: validateResetPass, trigger: 'blur' }]
}

const handleReset = async () => {
  const valid = await resetFormRef.value.validate().catch(() => false)
  if (!valid) return
  resetLoading.value = true
  try {
    const { data } = await axios.post('/api/resetPwd', resetForm)
    if (data.code === 200) {
      ElMessage.success(data.message || data.msg)
      mode.value = 'login'
    } else {
      ElMessage.error(data.message || data.msg)
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    resetLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
}

.login-card {
  width: 420px;
  padding: 40px 36px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  backdrop-filter: blur(20px);
}

/* 顶部标题 */
.card-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  border-radius: 16px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
}

.app-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 1px;
}

.app-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #909399;
}

/* 登录模式切换 */
.tabs {
  text-align: center;
  margin-bottom: 24px;
}

.tabs :deep(.el-radio-button__inner) {
  padding: 10px 32px;
  border-radius: 0;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 10px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  border: none;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
  transition: all 0.3s;
}

.login-btn:hover {
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.6);
  transform: translateY(-1px);
}

/* 注册/忘记密码链接 */
.links {
  display: flex;
  justify-content: space-between;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
  padding: 0 4px;
}

.links span {
  transition: color 0.2s;
}

.links span:hover {
  color: #337ecc;
  text-decoration: underline;
}

/* 注册/重置的按钮 — 用 flex 保证对齐 */
.btn-row {
  display: flex;
  gap: 10px;
}

.btn-row .el-button {
  flex: 1;
  height: 40px;
  border-radius: 10px;
  margin-left: 0;
}

.action-btn {
  background: linear-gradient(135deg, #409eff, #337ecc);
  border: none;
  letter-spacing: 4px;
}

.back-btn {
  /* 默认样式即可 */
}
</style>