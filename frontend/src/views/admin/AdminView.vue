<!--
============================================
文件: src/views/admin/AdminView.vue
============================================
-->
<template>
  <el-container class="admin-layout">
    <!-- 左侧菜单 -->
    <el-aside width="200px">
      <div class="logo">管理员后台</div>
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/parking">
          <el-icon><Van /></el-icon>
          <span>车位信息</span>
        </el-menu-item>
        <el-menu-item index="/admin/owners">
          <el-icon><User /></el-icon>
          <span>业主信息</span>
        </el-menu-item>
        <el-menu-item index="/admin/vehicles">
          <el-icon><Van /></el-icon>
          <span>车辆管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/payment">
          <el-icon><Bell /></el-icon>
          <span>缴费通知</span>
        </el-menu-item>
        <el-menu-item index="/admin/income-expenses">
          <el-icon><Money /></el-icon>
          <span>收支管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/repairs">
          <el-icon><Tools /></el-icon>
          <span>报修管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/parking-applications">
          <el-icon><DocumentChecked /></el-icon>
          <span>车位审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <!-- 右侧内容 -->
    <el-container>
      <el-header class="top-header">
        <div class="header-right">
          <el-button type="danger" size="small" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Van, User, Bell, DocumentChecked, DataBoard, Tools, Money } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const getHeaders = () => {
  const token = sessionStorage.getItem('token')
  return { headers: { token } }
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
        await axios.post('/api/admin/logout', {}, getHeaders())
      }
    } catch (e) {
      // 接口报错也继续退出
    }
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('token')
    router.push('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background-image: url('@/assets/images/adminbg.jpg');
  background-size: cover;
  background-position: center;
}
.logo { height: 60px; line-height: 60px; text-align: center; color: white; background: rgba(43, 47, 58, 0.88); }
.el-aside { background-color: rgba(48, 65, 86, 0.88) !important; }
.el-menu { background-color: transparent !important; }
.top-header {
  height: 50px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.3);
}
.header-right {
  display: flex;
  align-items: center;
}
.content {
  background: transparent;
  padding: 20px;
}
</style>