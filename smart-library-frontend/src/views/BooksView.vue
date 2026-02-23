<template>
  <div class="books-container">
    <el-container>
      <!-- 头部导航 -->
      <el-header>
        <div class="header-left">
          <el-button
              type="primary"
              :icon="ArrowLeft"
              @click="goBack"
              circle
              style="margin-right: 15px;"
          />
          <h1>图书列表</h1>
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
        <!-- 搜索区域 -->
        <el-card class="search-card">
          <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
            <el-form-item label="图书标题">
              <el-input v-model="searchForm.title" placeholder="请输入图书标题" clearable />
            </el-form-item>
            <el-form-item label="作者">
              <el-input v-model="searchForm.author" placeholder="请输入作者" clearable />
            </el-form-item>
            <el-form-item label="ISBN">
              <el-input v-model="searchForm.isbn" placeholder="请输入ISBN" clearable />
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
                <el-option label="编程" value="编程" />
                <el-option label="文学" value="文学" />
                <el-option label="历史" value="历史" />
                <el-option label="科技" value="科技" />
                <el-option label="艺术" value="艺术" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
              <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 图书列表 -->
        <el-card class="books-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>图书列表</span>
              <span class="total">共 {{ total }} 本</span>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="book in books" :key="book.id">
              <el-card shadow="hover" class="book-card" @click="showBookDetail(book)">
                <div class="book-cover">
                  <el-image
                    :src="book.coverUrl || '/default-book.png'"
                    fit="cover"
                    :lazy="true"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon :size="40"><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
                <div class="book-info">
                  <div class="book-title" :title="book.title">{{ book.title }}</div>
                  <div class="book-author">{{ book.author }}</div>
                  <div class="book-meta">
                    <el-tag size="small" type="info">{{ book.category }}</el-tag>
                    <el-tag size="small" :type="book.availableStock > 0 ? 'success' : 'danger'">
                      {{ book.availableStock > 0 ? `可借 ${book.availableStock}` : '暂无' }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="searchForm.pageNum"
              v-model:page-size="searchForm.pageSize"
              :page-sizes="[10, 20, 30, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSearch"
              @current-change="handleSearch"
            />
          </div>

          <!-- 空状态 -->
          <el-empty v-if="!loading && books.length === 0" description="暂无图书数据" />
        </el-card>
      </el-main>
    </el-container>

    <!-- 图书详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="selectedBook?.title"
      width="600px"
      destroy-on-close
    >
      <div class="book-detail" v-if="selectedBook">
        <div class="detail-cover">
          <el-image
            :src="selectedBook.coverUrl || '/default-book.png'"
            fit="cover"
          >
            <template #error>
              <div class="image-error">
                <el-icon :size="60"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <div class="detail-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ISBN">{{ selectedBook.isbn }}</el-descriptions-item>
            <el-descriptions-item label="作者">{{ selectedBook.author }}</el-descriptions-item>
            <el-descriptions-item label="出版社">{{ selectedBook.publisher }}</el-descriptions-item>
            <el-descriptions-item label="出版日期">{{ formatDate(selectedBook.publishDate) }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ selectedBook.category }}</el-descriptions-item>
            <el-descriptions-item label="价格">¥{{ selectedBook.price }}</el-descriptions-item>
            <el-descriptions-item label="总库存">{{ selectedBook.totalStock }}</el-descriptions-item>
            <el-descriptions-item label="可借数量">{{ selectedBook.availableStock }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(selectedBook.status)">
                {{ selectedBook.statusDescription }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="位置">{{ selectedBook.location || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div class="detail-description">
            <h4>图书简介</h4>
            <p>{{ selectedBook.description || '暂无简介' }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          type="primary"
          :disabled="selectedBook?.availableStock <= 0"
          :loading="borrowLoading"
          @click="handleBorrow"
        >
          {{ selectedBook?.availableStock > 0 ? '立即借阅' : '暂无库存' }}
        </el-button>
        <el-button
          v-if="selectedBook?.availableStock <= 0"
          type="warning"
          :loading="reserveLoading"
          @click="handleReserve"
        >
          预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown, Search, Refresh, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { searchBooks, getBookById } from '@/api/book'
import { borrowBook } from '@/api/borrow'
import { reserveBook } from '@/api/reservation'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const goBack = () => {
  router.back() // 返回上一页
}

const loading = ref(false)
const borrowLoading = ref(false)
const reserveLoading = ref(false)
const detailVisible = ref(false)
const books = ref([])
const total = ref(0)
const selectedBook = ref(null)

const searchForm = reactive({
  title: '',
  author: '',
  isbn: '',
  category: '',
  pageNum: 1,
  pageSize: 10
})

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await searchBooks(searchForm)
    books.value = res.content || res
    total.value = res.totalElements || res.length
  } catch (error) {
    ElMessage.error(error.message || '获取图书列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchBooks()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    author: '',
    isbn: '',
    category: '',
    pageNum: 1,
    pageSize: 10
  })
  fetchBooks()
}

// 显示图书详情
const showBookDetail = async (book) => {
  selectedBook.value = book
  detailVisible.value = true
}

// 借书
const handleBorrow = async () => {
  if (!selectedBook.value) return

  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (userStore.userInfo?.availableBorrowCount <= 0) {
    ElMessage.warning('已达到最大借阅数量，请先归还部分图书')
    return
  }

  try {
    await ElMessageBox.confirm(`确认借阅《${selectedBook.value.title}》吗？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })

    borrowLoading.value = true
    await borrowBook({
      bookId: selectedBook.value.id,
      borrowDays: 30
    })

    ElMessage.success('借阅成功')
    detailVisible.value = false
    fetchBooks()
    // 刷新用户信息
    await userStore.fetchUserInfo()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '借阅失败')
    }
  } finally {
    borrowLoading.value = false
  }
}

// 预约
const handleReserve = async () => {
  if (!selectedBook.value) return

  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await ElMessageBox.confirm(`确认预约《${selectedBook.value.title}》吗？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    reserveLoading.value = true
    await reserveBook({
      bookId: selectedBook.value.id,
      remark: ''
    })

    ElMessage.success('预约成功')
    detailVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '预约失败')
    }
  } finally {
    reserveLoading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
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
  // 检查是否有查询参数
  if (route.query.title) {
    searchForm.title = route.query.title
  }
  if (route.query.bookId) {
    getBookById(route.query.bookId).then(res => {
      selectedBook.value = res
      detailVisible.value = true
    })
  }
  fetchBooks()
})
</script>

<style scoped>
.books-container {
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

.search-card {
  margin-bottom: 20px;
}

.books-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total {
  font-size: 14px;
  color: #909399;
}

.book-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.book-cover {
  width: 100%;
  padding-top: 140%;
  position: relative;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}

.book-cover .el-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #c0c4cc;
}

.book-info {
  padding: 12px 0 0;
}

.book-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.book-author {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.book-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.book-detail {
  display: flex;
  gap: 30px;
}

.detail-cover {
  width: 200px;
  flex-shrink: 0;
}

.detail-cover .el-image {
  width: 100%;
  aspect-ratio: 2/3;
  border-radius: 8px;
  overflow: hidden;
}

.detail-info {
  flex: 1;
}

.detail-description {
  margin-top: 20px;
}

.detail-description h4 {
  margin-bottom: 10px;
  color: #333;
}

.detail-description p {
  color: #666;
  line-height: 1.8;
}
</style>
