import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'

const OwnerView = () => import('@/views/owner/OwnerView.vue')
const AdminView = () => import('@/views/admin/AdminView.vue')
const AdminDashboard = () => import('@/views/admin/AdminDashboard.vue')
const AdminParkingMgmt = () => import('@/views/admin/ParkingSpotMgmt.vue')
const OwnerList = () => import('@/views/admin/OwnerList.vue')
const VehicleMgmt = () => import('@/views/admin/VehicleMgmt.vue')
const PaymentMgmt = () => import('@/views/admin/PaymentMgmt.vue')
const RepairMgmt = () => import('@/views/admin/RepairMgmt.vue')
const IncomeExpenseMgmt = () => import('@/views/admin/IncomeExpenseMgmt.vue')
const OwnerParking = () => import('@/views/owner/OwnerParking.vue')
const ParkingApplicationMgmt = () => import('@/views/admin/ParkingApplicationMgmt.vue')
const routes = [
  { path: '/login', name: 'Login', component: Login },
  {
    path: '/owner',
    component: OwnerView,
    redirect: '/owner/home',
    children: [
      { path: 'home', component: { template: '<div>这是业主端</div>' } },
      { path: 'parking', name: 'OwnerParking', component: OwnerParking }
    ]
  },
  {
    path: '/admin',
    component: AdminView,
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: AdminDashboard },
      { path: 'parking', name: 'AdminParking', component: AdminParkingMgmt },
      { path: 'owners', name: 'AdminOwners', component: OwnerList },
      { path: 'vehicles', name: 'AdminVehicles', component: VehicleMgmt },
      { path: 'payment', name: 'AdminPayment', component: PaymentMgmt },
      { path: 'income-expenses', name: 'AdminIncomeExpenses', component: IncomeExpenseMgmt },
      { path: 'repairs', name: 'AdminRepairs', component: RepairMgmt },
      { path: 'parking-applications', name: 'AdminParkingApplications', component: ParkingApplicationMgmt }
    ]
  },
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router