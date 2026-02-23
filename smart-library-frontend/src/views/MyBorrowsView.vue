<template>
  <div class="my-borrows-container">
    <el-container>
      <!-- 头部导航 -->
      <el-header>
        <div class="header-left">
          <h1>我的借阅</h1>
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
                <el-icon :size="50" color="#409eff"><Reading /></el-icon>
                <div class="stat-content">
                  <div class="stat-value">{{ borrowStats.current }}</div>
                  <div class="stat-label">当前借阅</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div class="stat-item">
                <el-icon :size="50" color="#e6a23c"><Warning /></el-icon>
                <div class="stat-content">
                  <div class="stat-value">{{ borrowStats.overdue }}</div>
                  <div class="stat-label">逾期未还</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div class="stat-item">
                <el-icon :size="50" color="#67c23a"><SuccessFilled /></el-icon>
                <div class="stat-content">
                  <div class="stat-value">{{ borrowStats.returned }}</div>
                  <div class="stat-label">已归还</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 借阅记录列表 -->
        <el-card class="borrows-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>借阅记录</span>
            </div>
          </template>

          <el-table :data="borrowRecords" stripe>
            <el-table-column prop="bookTitle" label="图书名称" min-width="200">
              <template #default="{ row }">
                <div class="book-cell">
                  <el-icon><Reading /></el-icon>
                  <span>{{ row.bookTitle }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="bookIsbn" label="ISBN" width="150" />
            <el-table-column prop="borrowDate" label="借阅日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.borrowDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="dueDate" label="应还日期" width="120">
              <template #default="{ row }">
                <span :class="{ 'overdue-date': row.isOverdue }">
                  {{ formatDate(row.dueDate) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="returnDate" label="归还日期" width="120">
              <template #default="{ row }">
                {{ row.returnDate ? formatDate(row.returnDate) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="renewCount" label="续借次数" width="100" align="center">
              <template #default="{ row }">
                {{ row.renewCount }} / {{ row.maxRenewCount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ row.statusDescription }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="fineAmount" label="罚款" width="100" align="right">
              <template #default="{ row }">
                <span :class="{ 'fine-amount': row.fineAmount > 0 }">
                  {{ row.fineAmount > 0 ? `¥${row.fineAmount}` : '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'BORROWED' && !row.isOverdue && row.renewCount < row.maxRenewCount"
                  type="primary"
                  size="small"
                  :loading="row.renewLoading"
                  @click="handleRenew(row)"
                >
                  续借
                </el-button>
                <el-button
                  v-if="row.status === 'BORROWED'"
                  type="warning"
                  size="small"
                  :loading="row.returnLoading"
                  @click="handleReturn(row)"
                >
                  还书
                </el-button>
                <el-tag v-else-if="row.status === 'RETURNED'" type="success" size="small">
                  已归还
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
              @size-change="fetchBorrowRecords"
              @current-change="fetchBorrowRecords"
            />
          </div>

          <!-- 空状态 -->
          <el-empty v-if="!loading && borrowRecords.length === 0" description="暂无借阅记录" />
        </el-card>
      </el-main>
    </el-container>

    <!-- 还书对话框 -->
    <el-dialog v-model="returnVisible" title="还书确认" width="500px">
      <el-form :model="returnForm" label-width="80px">
        <el-form-item label="图书名称">
          <span>{{ currentRecord?.bookTitle }}</span>
        </el-form-item>
        <el-form-item label="借阅日期">
          <span>{{ formatDate(currentRecord?.borrowDate) }}</span>
        </el-form-item>
        <el-form-item label="应还日期">
          <span>{{ formatDate(currentRecord?.dueDate) }}</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="returnForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（如图书损坏情况等）"
          />
        </el-form-item>
        <el-form-item v-if="currentRecord?.isOverdue" label="逾期信息">
          <el-alert
            :title="`已逾期 ${currentRecord.overdueDays} 天，罚款 ¥${currentRecord.fineAmount}`"
            type="warning"
            :closable="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" :loading="returnLoading" @click="confirmReturn">确认还书</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown, Reading, Warning, SuccessFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyBorrowRecords, renewBook } from '@/api/borrow'
import { returnBook } from '@/api/borrow'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const returnLoading = ref(false)
const returnVisible = ref(false)
const borrowRecords = ref([])
const total = ref(0)
const currentRecord = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const returnForm = reactive({
  remark: ''
})

// 统计数据
const borrowStats = computed(() => {
  const stats = {
    current: 0,
    overdue: 0,
    returned: 0
  }
  borrowRecords.value.forEach(record => {
    if (record.status === 'BORROWED') {
      stats.current++
      if (record.isOverdue) {
        stats.overdue++
      }
    } else if (record.status === 'RETURNED') {
      stats.returned++
    }
  })
  return stats
})

// 获取借阅记录
const fetchBorrowRecords = async () => {
  loading.value = true
  try {
    const res = await getMyBorrowRecords(pagination)
    borrowRecords.value = res.content || res
    total.value = res.totalElements || res.length
  } catch (error) {
    ElMessage.error(error.message || '获取借阅记录失败')
  } finally {
    loading.value = false
  }
}

// 续借
const handleRenew = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确认续借《${record.bookTitle}》吗？续借后将延长30天借阅期限。`,
      '续借确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    record.renewLoading = true
    await renewBook(record.id)
    ElMessage.success('续借成功')
    await fetchBorrowRecords()
    await userStore.fetchUserInfo()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '续借失败')
    }
  } finally {
    record.renewLoading = false
  }
}

// 显示还书对话框
const handleReturn = (record) => {
  currentRecord.value = record
  returnForm.remark = ''
  returnVisible.value = true
}

// 确认还书
const confirmReturn = async () => {
  if (!currentRecord.value) return

  returnLoading.value = true
  try {
    await returnBook({
      borrowRecordId: currentRecord.value.id,
      remark: returnForm.remark
    })
    ElMessage.success('还书成功')
    returnVisible.value = false
    await fetchBorrowRecords()
    await userStore.fetchUserInfo()
  } catch (error) {
    ElMessage.error(error.message || '还书失败')
  } finally {
    returnLoading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'BORROWED': 'warning',
    'RETURNED': 'success',
    'OVERDUE': 'danger',
    'LOST': 'info'
  }
  return typeMap[status] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
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
  fetchBorrowRecords()
})
</script>

<style scoped>
.my-borrows-container {
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

.borrows-card {
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

.overdue-date {
  color: #f56c6c;
  font-weight: 500;
}

.fine-amount {
  color: #f56c6c;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
