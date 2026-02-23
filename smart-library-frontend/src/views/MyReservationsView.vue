<template>
  <div class="my-reservations-container">
    <el-container>
      <!-- 头部导航 -->
      <el-header>
        <div class="header-left">
          <h1>我的预约</h1>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo?.realName || '用户' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="home">返回首页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主体内容 -->
      <el-main>
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="8">
            <el-card shadow="hover">
              <div class="stat-item">
                <el-icon :size="50" color="#409eff"><Clock /></el-icon>
                <div class="stat-content">
                  <div class="stat-value">{{ reservationStats.pending }}</div>
                  <div class="stat-label">等待中</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div class="stat-item">
                <el-icon :size="50" color="#67c23a"><Bell /></el-icon>
                <div class="stat-content">
                  <div class="stat-value">{{ reservationStats.active }}</div>
                  <div class="stat-label">可借阅</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div class="stat-item">
                <el-icon :size="50" color="#909399"><CircleClose /></el-icon>
                <div class="stat-content">
                  <div class="stat-value">{{ reservationStats.cancelled }}</div>
                  <div class="stat-label">已取消</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 预约记录列表 -->
        <el-card class="reservations-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>预约记录</span>
            </div>
          </template>

          <el-table :data="reservationRecords" stripe>
            <el-table-column prop="bookTitle" label="图书名称" min-width="200">
              <template #default="{ row }">
                <div class="book-cell">
                  <el-icon><Reading /></el-icon>
                  <span>{{ row.bookTitle }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="bookIsbn" label="ISBN" width="150" />
            <el-table-column prop="reservationDate" label="预约时间" width="120">
              <template #default="{ row }">
                {{ formatDateTime(row.reservationDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="expireTime" label="过期时间" width="120">
              <template #default="{ row }">
                <span :class="{ 'expire-soon': isExpireSoon(row.expireTime) }">
                  {{ formatDateTime(row.expireTime) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="queuePosition" label="队列位置" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="info">第 {{ row.queuePosition }} 位</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="notificationStatus" label="通知状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.notificationStatus === 'NOTIFIED' ? 'success' : 'info'">
                  {{ row.notificationStatusDescription }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ row.statusDescription }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PENDING' || row.status === 'ACTIVE'"
                  type="danger"
                  size="small"
                  :loading="row.cancelLoading"
                  @click="handleCancel(row)"
                >
                  取消预约
                </el-button>
                <el-button
                  v-if="row.status === 'ACTIVE'"
                  type="primary"
                  size="small"
                  @click="goToBorrow(row)"
                >
                  去借阅
                </el-button>
                <el-tag v-else-if="row.status === 'EXPIRED'" type="info" size="small">
                  已过期
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 30, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchReservations"
              @current-change="fetchReservations"
            />
          </div>

          <!-- 空状态 -->
          <el-empty v-if="!loading && reservationRecords.length === 0" description="暂无预约记录" />
        </el-card>
      </el-main>
    </el-container>

    <!-- 取消预约对话框 -->
    <el-dialog v-model="cancelVisible" title="取消预约" width="500px">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="图书名称">
          <span>{{ currentRecord?.bookTitle }}</span>
        </el-form-item>
        <el-form-item label="队列位置">
          <span>第 {{ currentRecord?.queuePosition }} 位</span>
        </el-form-item>
        <el-form-item label="取消原因">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="danger" :loading="cancelLoading" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown, Clock, Bell, CircleClose, Reading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyReservations } from '@/api/reservation'
import { cancelReservation } from '@/api/reservation'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const cancelLoading = ref(false)
const cancelVisible = ref(false)
const reservationRecords = ref([])
const total = ref(0)
const currentRecord = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const cancelForm = reactive({
  reason: ''
})

// 统计数据
const reservationStats = computed(() => {
  const stats = {
    pending: 0,
    active: 0,
    cancelled: 0
  }
  reservationRecords.value.forEach(record => {
    if (record.status === 'PENDING') {
      stats.pending++
    } else if (record.status === 'ACTIVE') {
      stats.active++
    } else if (record.status === 'CANCELLED' || record.status === 'EXPIRED') {
      stats.cancelled++
    }
  })
  return stats
})

// 获取预约记录
const fetchReservations = async () => {
  loading.value = true
  try {
    const res = await getMyReservations(pagination)
    reservationRecords.value = res.content || res
    total.value = res.totalElements || res.length
  } catch (error) {
    ElMessage.error(error.message || '获取预约记录失败')
  } finally {
    loading.value = false
  }
}

// 取消预约
const handleCancel = (record) => {
  currentRecord.value = record
  cancelForm.reason = ''
  cancelVisible.value = true
}

// 确认取消
const confirmCancel = async () => {
  if (!currentRecord.value) return

  cancelLoading.value = true
  try {
    await cancelReservation(currentRecord.value.id, { reason: cancelForm.reason })
    ElMessage.success('取消成功')
    cancelVisible.value = false
    await fetchReservations()
  } catch (error) {
    ElMessage.error(error.message || '取消失败')
  } finally {
    cancelLoading.value = false
  }
}

// 去借阅
const goToBorrow = (record) => {
  ElMessage.info('请在图书列表页面借阅该图书')
  router.push({
    path: '/books',
    query: { bookId: record.bookId }
  })
}

// 检查是否即将过期（24小时内）
const isExpireSoon = (expireTime) => {
  if (!expireTime) return false
  const now = new Date()
  const expire = new Date(expireTime)
  const diff = expire - now
  return diff > 0 && diff < 24 * 60 * 60 * 1000
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'ACTIVE': 'success',
    'CANCELLED': 'warning',
    'COMPLETED': 'success',
    'EXPIRED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.toLocaleDateString('zh-CN')} ${d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
}

// 下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'home':
      router.push('/home')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
      break
  }
}

onMounted(() => {
  fetchReservations()
})
</script>

<style scoped>
.my-reservations-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 40px;
}

.header-left h1 {
  margin: 0;
  font-size: 24px;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.el-main {
  padding: 20px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.reservations-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.expire-soon {
  color: #e6a23c;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
