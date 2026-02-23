<template>
  <div class="profile-container">
    <el-container>
      <!-- 头部导航 -->
      <el-header>
        <div class="header-left">
          <h1>个人中心</h1>
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
                <el-dropdown-item command="home">返回首页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主体内容 -->
      <el-main>
        <el-row :gutter="20">
          <!-- 左侧：用户信息 -->
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>基本信息</span>
                  <el-button type="primary" size="small" :icon="Edit" @click="editVisible = true">
                    编辑
                  </el-button>
                </div>
              </template>

              <div class="profile-info">
                <div class="avatar">
                  <el-icon :size="80"><User /></el-icon>
                </div>
                <h3>{{ userInfo?.realName || '用户' }}</h3>
                <el-tag :type="userInfo?.role === 'ADMIN' ? 'danger' : 'primary'" size="large">
                  {{ userInfo?.roleDescription || '读者' }}
                </el-tag>

                <el-divider />

                <el-descriptions :column="1" border>
                  <el-descriptions-item label="用户名">{{ userInfo?.username }}</el-descriptions-item>
                  <el-descriptions-item label="手机号">{{ userInfo?.phone || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="邮箱">{{ userInfo?.email || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="账号状态">
                    <el-tag :type="userInfo?.status === 'ACTIVE' ? 'success' : 'danger'">
                      {{ userInfo?.statusDescription }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="注册时间">
                    {{ formatDate(userInfo?.createdAt) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="最后登录">
                    {{ formatDateTime(userInfo?.lastLoginAt) }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-card>

            <!-- 借阅统计 -->
            <el-card shadow="hover" style="margin-top: 20px;">
              <template #header>
                <span>借阅统计</span>
              </template>

              <div class="borrow-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ userInfo?.maxBorrowCount }}</div>
                  <div class="stat-label">最大借阅</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-value">{{ userInfo?.currentBorrowCount }}</div>
                  <div class="stat-label">当前借阅</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-value" :style="{ color: userInfo?.availableBorrowCount > 0 ? '#67c23a' : '#f56c6c' }">
                    {{ userInfo?.availableBorrowCount }}
                  </div>
                  <div class="stat-label">可借数量</div>
                </div>
              </div>

              <!-- 借阅进度条 -->
              <div class="borrow-progress">
                <div class="progress-label">
                  <span>借阅进度</span>
                  <span>{{ userInfo?.currentBorrowCount }} / {{ userInfo?.maxBorrowCount }}</span>
                </div>
                <el-progress
                  :percentage="borrowPercentage"
                  :color="progressColor"
                  :stroke-width="20"
                />
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：操作记录 -->
          <el-col :span="16">
            <el-card shadow="hover">
              <template #header>
                <el-tabs v-model="activeTab">
                  <el-tab-pane label="借阅记录" name="borrows" />
                  <el-tab-pane label="预约记录" name="reservations" />
                </el-tabs>
              </template>

              <!-- 借阅记录 -->
              <div v-if="activeTab === 'borrows'" v-loading="borrowLoading">
                <el-empty v-if="recentBorrows.length === 0" description="暂无借阅记录" />
                <el-timeline v-else>
                  <el-timeline-item
                    v-for="record in recentBorrows"
                    :key="record.id"
                    :timestamp="formatDateTime(record.borrowDate)"
                    placement="top"
                    :type="getTimelineType(record.status)"
                  >
                    <el-card>
                      <div class="record-item">
                        <div class="record-header">
                          <h4>{{ record.bookTitle }}</h4>
                          <el-tag :type="getStatusType(record.status)" size="small">
                            {{ record.statusDescription }}
                          </el-tag>
                        </div>
                        <div class="record-meta">
                          <span>借阅日期：{{ formatDate(record.borrowDate) }}</span>
                          <span>应还日期：{{ formatDate(record.dueDate) }}</span>
                          <span v-if="record.isOverdue" style="color: #f56c6c;">
                            已逾期 {{ record.overdueDays }} 天，罚款 ¥{{ record.fineAmount }}
                          </span>
                        </div>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>

              <!-- 预约记录 -->
              <div v-if="activeTab === 'reservations'" v-loading="reservationLoading">
                <el-empty v-if="recentReservations.length === 0" description="暂无预约记录" />
                <el-timeline v-else>
                  <el-timeline-item
                    v-for="record in recentReservations"
                    :key="record.id"
                    :timestamp="formatDateTime(record.reservationDate)"
                    placement="top"
                    :type="getTimelineType(record.status)"
                  >
                    <el-card>
                      <div class="record-item">
                        <div class="record-header">
                          <h4>{{ record.bookTitle }}</h4>
                          <el-tag :type="getStatusType(record.status)" size="small">
                            {{ record.statusDescription }}
                          </el-tag>
                        </div>
                        <div class="record-meta">
                          <span>队列位置：第 {{ record.queuePosition }} 位</span>
                          <span>过期时间：{{ formatDateTime(record.expireTime) }}</span>
                        </div>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <!-- 编辑个人信息对话框 -->
    <el-dialog v-model="editVisible" title="编辑个人信息" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, ArrowDown, Edit } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateProfile } from '@/api/user'
import { getMyBorrowRecords } from '@/api/borrow'
import { getMyReservations } from '@/api/reservation'

const router = useRouter()
const userStore = useUserStore()

const editVisible = ref(false)
const editFormRef = ref(null)
const saveLoading = ref(false)
const borrowLoading = ref(false)
const reservationLoading = ref(false)
const activeTab = ref('borrows')

const userInfo = computed(() => userStore.userInfo)

const editForm = reactive({
  realName: '',
  phone: '',
  email: ''
})

const editRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const recentBorrows = ref([])
const recentReservations = ref([])

// 借阅进度
const borrowPercentage = computed(() => {
  if (!userInfo.value) return 0
  return Math.round((userInfo.value.currentBorrowCount / userInfo.value.maxBorrowCount) * 100)
})

// 进度条颜色
const progressColor = computed(() => {
  const percentage = borrowPercentage.value
  if (percentage >= 100) return '#f56c6c'
  if (percentage >= 80) return '#e6a23c'
  return '#409eff'
})

// 获取最近借阅记录
const fetchRecentBorrows = async () => {
  borrowLoading.value = true
  try {
    const res = await getMyBorrowRecords({ pageNum: 1, pageSize: 5 })
    recentBorrows.value = res.content || res
  } catch (error) {
    console.error('获取借阅记录失败:', error)
  } finally {
    borrowLoading.value = false
  }
}

// 获取最近预约记录
const fetchRecentReservations = async () => {
  reservationLoading.value = true
  try {
    const res = await getMyReservations({ pageNum: 1, pageSize: 5 })
    recentReservations.value = res.content || res
  } catch (error) {
    console.error('获取预约记录失败:', error)
  } finally {
    reservationLoading.value = false
  }
}

// 打开编辑对话框
const handleEdit = () => {
  editForm.realName = userInfo.value.realName
  editForm.phone = userInfo.value.phone || ''
  editForm.email = userInfo.value.email || ''
  editVisible.value = true
}

// 保存个人信息
const handleSave = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        await updateProfile(editForm)
        ElMessage.success('保存成功')
        editVisible.value = false
        await userStore.fetchUserInfo()
      } catch (error) {
        ElMessage.error(error.message || '保存失败')
      } finally {
        saveLoading.value = false
      }
    }
  })
}

// 获取时间线类型
const getTimelineType = (status) => {
  const typeMap = {
    'BORROWED': 'primary',
    'RETURNED': 'success',
    'OVERDUE': 'danger',
    'PENDING': 'info',
    'ACTIVE': 'warning',
    'CANCELLED': 'info',
    'COMPLETED': 'success',
    'EXPIRED': 'danger'
  }
  return typeMap[status] || 'primary'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'BORROWED': 'warning',
    'RETURNED': 'success',
    'OVERDUE': 'danger',
    'PENDING': 'info',
    'ACTIVE': 'success',
    'CANCELLED': 'warning',
    'COMPLETED': 'success',
    'EXPIRED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
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

onMounted(async () => {
  await userStore.fetchUserInfo()
  fetchRecentBorrows()
  fetchRecentReservations()
})
</script>

<style scoped>
.profile-container {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  color: white;
}

.profile-info h3 {
  margin: 10px 0;
  font-size: 20px;
  color: #333;
}

.borrow-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.stat-divider {
  width: 1px;
  background-color: #e4e7ed;
}

.borrow-progress {
  margin-top: 20px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.record-item {
  padding: 10px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.record-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.record-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
  flex-wrap: wrap;
}
</style>
