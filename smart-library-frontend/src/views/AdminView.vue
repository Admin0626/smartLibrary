<template>
  <div class="admin-container">
    <el-container>
      <!-- 头部导航 -->
      <el-header>
        <div class="header-left">
          <h1>管理后台</h1>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo?.realName || '管理员' }}
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

      <!-- 侧边栏 -->
      <el-container>
        <el-aside width="200px">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="dashboard">
              <el-icon><DataBoard /></el-icon>
              <span>数据概览</span>
            </el-menu-item>
            <el-menu-item index="books">
              <el-icon><Reading /></el-icon>
              <span>图书管理</span>
            </el-menu-item>
            <el-menu-item index="users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="borrows">
              <el-icon><Tickets /></el-icon>
              <span>借阅管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main>
          <!-- 数据概览 -->
          <div v-if="activeMenu === 'dashboard'">
            <el-row :gutter="20" class="stats-row">
              <el-col :span="6">
                <el-card shadow="hover">
                  <div class="stat-card">
                    <el-icon :size="50" color="#409eff"><Reading /></el-icon>
                    <div class="stat-content">
                      <div class="stat-value">{{ dashboardStats.totalBooks }}</div>
                      <div class="stat-label">图书总数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover">
                  <div class="stat-card">
                    <el-icon :size="50" color="#67c23a"><User /></el-icon>
                    <div class="stat-content">
                      <div class="stat-value">{{ dashboardStats.totalUsers }}</div>
                      <div class="stat-label">用户总数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover">
                  <div class="stat-card">
                    <el-icon :size="50" color="#e6a23c"><Tickets /></el-icon>
                    <div class="stat-content">
                      <div class="stat-value">{{ dashboardStats.activeBorrows }}</div>
                      <div class="stat-label">当前借阅</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover">
                  <div class="stat-card">
                    <el-icon :size="50" color="#f56c6c"><Warning /></el-icon>
                    <div class="stat-content">
                      <div class="stat-value">{{ dashboardStats.overdueBooks }}</div>
                      <div class="stat-label">逾期未还</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-card shadow="hover" style="margin-top: 20px;">
              <template #header>
                <span>快捷操作</span>
              </template>
              <div class="quick-actions">
                <el-button type="primary" :icon="Plus" @click="showAddBookDialog">
                  添加图书
                </el-button>
                <el-button type="warning" :icon="Refresh" @click="fetchDashboardStats">
                  刷新数据
                </el-button>
              </div>
            </el-card>
          </div>

          <!-- 图书管理 -->
          <div v-if="activeMenu === 'books'">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>图书列表</span>
                  <el-button type="primary" :icon="Plus" @click="showAddBookDialog">
                    添加图书
                  </el-button>
                </div>
              </template>
              <el-table :data="books" stripe v-loading="booksLoading">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="title" label="书名" min-width="200" />
                <el-table-column prop="author" label="作者" width="120" />
                <el-table-column prop="isbn" label="ISBN" width="150" />
                <el-table-column prop="category" label="分类" width="100" />
                <el-table-column prop="totalStock" label="总库存" width="100" align="center" />
                <el-table-column prop="availableStock" label="可借" width="100" align="center" />
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getBookStatusType(row.status)">
                      {{ row.statusDescription }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" @click="editBook(row)">
                      编辑
                    </el-button>
                    <el-button type="danger" size="small" @click="deleteBook(row)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination">
                <el-pagination
                  v-model:current-page="booksPagination.pageNum"
                  v-model:page-size="booksPagination.pageSize"
                  :total="booksTotal"
                  layout="total, prev, pager, next"
                  @current-change="fetchBooks"
                />
              </div>
            </el-card>
          </div>

          <!-- 用户管理 -->
          <div v-if="activeMenu === 'users'">
            <el-card shadow="hover">
              <template #header>
                <span>用户列表</span>
              </template>
              <el-table :data="users" stripe v-loading="usersLoading">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column prop="realName" label="真实姓名" width="120" />
                <el-table-column prop="phone" label="手机号" width="120" />
                <el-table-column prop="email" label="邮箱" width="180" />
                <el-table-column prop="role" label="角色" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
                      {{ row.roleDescription }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                      {{ row.statusDescription }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination">
                <el-pagination
                  v-model:current-page="usersPagination.pageNum"
                  v-model:page-size="usersPagination.pageSize"
                  :total="usersTotal"
                  layout="total, prev, pager, next"
                  @current-change="fetchUsers"
                />
              </div>
            </el-card>
          </div>

          <!-- 借阅管理 -->
          <div v-if="activeMenu === 'borrows'">
            <el-card shadow="hover">
              <template #header>
                <span>逾期借阅记录</span>
              </template>
              <el-table :data="overdueRecords" stripe v-loading="borrowsLoading">
                <el-table-column prop="id" label="记录ID" width="100" />
                <el-table-column prop="username" label="用户" width="120" />
                <el-table-column prop="bookTitle" label="图书" min-width="200" />
                <el-table-column prop="borrowDate" label="借阅日期" width="120">
                  <template #default="{ row }">
                    {{ formatDate(row.borrowDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="dueDate" label="应还日期" width="120">
                  <template #default="{ row }">
                    {{ formatDate(row.dueDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="overdueDays" label="逾期天数" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag type="danger">{{ row.overdueDays }} 天</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="fineAmount" label="罚款" width="100" align="right">
                  <template #default="{ row }">
                    <span style="color: #f56c6c;">¥{{ row.fineAmount }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 添加/编辑图书对话框 -->
    <el-dialog
      v-model="bookDialogVisible"
      :title="isEditMode ? '编辑图书' : '添加图书'"
      width="600px"
    >
      <el-form :model="bookForm" :rules="bookRules" ref="bookFormRef" label-width="100px">
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="书名" prop="title">
          <el-input v-model="bookForm.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="出版日期" prop="publishDate">
          <el-date-picker
            v-model="bookForm.publishDate"
            type="date"
            placeholder="选择出版日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="bookForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="编程" value="编程" />
            <el-option label="文学" value="文学" />
            <el-option label="历史" value="历史" />
            <el-option label="科技" value="科技" />
            <el-option label="艺术" value="艺术" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="bookForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总库存" prop="totalStock">
          <el-input-number v-model="bookForm.totalStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="封面URL" prop="coverUrl">
          <el-input v-model="bookForm.coverUrl" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input
            v-model="bookForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入图书简介"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bookSaveLoading" @click="saveBook">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, ArrowDown, DataBoard, Reading, Tickets, Plus, Refresh, Warning
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getAllUsers } from '@/api/user'
import { searchBooks, createBook, updateBook, deleteBook as deleteBookApi } from '@/api/book'
import { getOverdueRecords } from '@/api/borrow'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('dashboard')

// 数据概览
const dashboardStats = reactive({
  totalBooks: 0,
  totalUsers: 0,
  activeBorrows: 0,
  overdueBooks: 0
})

// 图书管理
const books = ref([])
const booksLoading = ref(false)
const booksTotal = ref(0)
const booksPagination = reactive({ pageNum: 1, pageSize: 10 })

// 用户管理
const users = ref([])
const usersLoading = ref(false)
const usersTotal = ref(0)
const usersPagination = reactive({ pageNum: 1, pageSize: 10 })

// 借阅管理
const overdueRecords = ref([])
const borrowsLoading = ref(false)

// 图书对话框
const bookDialogVisible = ref(false)
const bookFormRef = ref(null)
const bookSaveLoading = ref(false)
const isEditMode = ref(false)
const editingBookId = ref(null)

const bookForm = reactive({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishDate: '',
  category: '',
  price: 0,
  totalStock: 0,
  coverUrl: '',
  description: ''
})

const bookRules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  publisher: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  totalStock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// 获取数据概览
const fetchDashboardStats = async () => {
  try {
    // 获取图书总数
    const booksRes = await searchBooks({ pageNum: 1, pageSize: 1 })
    dashboardStats.totalBooks = booksRes.totalElements || 0

    // 获取用户总数
    const usersRes = await getAllUsers({ pageNum: 1, pageSize: 1 })
    dashboardStats.totalUsers = usersRes.totalElements || 0

    // 获取逾期记录
    const overdueRes = await getOverdueRecords()
    dashboardStats.overdueBooks = overdueRes.length
  } catch (error) {
    console.error('获取数据概览失败:', error)
  }
}

// 获取图书列表
const fetchBooks = async () => {
  booksLoading.value = true
  try {
    const res = await searchBooks(booksPagination)
    books.value = res.content || res
    booksTotal.value = res.totalElements || 0
  } catch (error) {
    ElMessage.error(error.message || '获取图书列表失败')
  } finally {
    booksLoading.value = false
  }
}

// 获取用户列表
const fetchUsers = async () => {
  usersLoading.value = true
  try {
    const res = await getAllUsers(usersPagination)
    users.value = res.content || res
    usersTotal.value = res.totalElements || 0
  } catch (error) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    usersLoading.value = false
  }
}

// 获取逾期记录
const fetchOverdueRecords = async () => {
  borrowsLoading.value = true
  try {
    const res = await getOverdueRecords()
    overdueRecords.value = res
  } catch (error) {
    ElMessage.error(error.message || '获取逾期记录失败')
  } finally {
    borrowsLoading.value = false
  }
}

// 显示添加图书对话框
const showAddBookDialog = () => {
  isEditMode.value = false
  editingBookId.value = null
  Object.assign(bookForm, {
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishDate: '',
    category: '',
    price: 0,
    totalStock: 0,
    coverUrl: '',
    description: ''
  })
  bookDialogVisible.value = true
}

// 编辑图书
const editBook = (book) => {
  isEditMode.value = true
  editingBookId.value = book.id
  Object.assign(bookForm, book)
  bookDialogVisible.value = true
}

// 保存图书
const saveBook = async () => {
  if (!bookFormRef.value) return

  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      bookSaveLoading.value = true
      try {
        if (isEditMode.value) {
          await updateBook(editingBookId.value, bookForm)
          ElMessage.success('更新成功')
        } else {
          await createBook(bookForm)
          ElMessage.success('添加成功')
        }
        bookDialogVisible.value = false
        await fetchBooks()
        await fetchDashboardStats()
      } catch (error) {
        ElMessage.error(error.message || '保存失败')
      } finally {
        bookSaveLoading.value = false
      }
    }
  })
}

// 删除图书
const deleteBook = async (book) => {
  try {
    await ElMessageBox.confirm(
      `确认删除《${book.title}》吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteBookApi(book.id)
    ElMessage.success('删除成功')
    await fetchBooks()
    await fetchDashboardStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 获取图书状态类型
const getBookStatusType = (status) => {
  const typeMap = {
    'AVAILABLE': 'success',
    'BORROWED': 'warning',
    'RESERVED': 'info',
    'MAINTENANCE': 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'dashboard') fetchDashboardStats()
  if (index === 'books') fetchBooks()
  if (index === 'users') fetchUsers()
  if (index === 'borrows') fetchOverdueRecords()
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

onMounted(() => {
  fetchDashboardStats()
})
</script>

<style scoped>
.admin-container {
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

.el-aside {
  background: white;
  border-right: 1px solid #e4e7ed;
}

.el-main {
  padding: 20px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
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

.quick-actions {
  display: flex;
  gap: 15px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
