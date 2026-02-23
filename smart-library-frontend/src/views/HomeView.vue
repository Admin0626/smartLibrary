<template>
  <div class="home-container">
    <el-container>
      <!-- 头部导航 -->
      <el-header>
        <div class="header-left">
          <h1>智慧图书馆</h1>
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
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主体内容 -->
      <el-main>
        <!-- 搜索区域 -->
        <div class="search-section">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索图书标题、作者、ISBN..."
            size="large"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- 快捷入口 -->
        <div class="quick-links">
          <el-card shadow="hover" @click="router.push('/books')">
            <div class="quick-link-item">
              <el-icon :size="40"><Reading /></el-icon>
              <span>图书列表</span>
            </div>
          </el-card>
          <el-card shadow="hover" @click="router.push('/my-borrows')">
            <div class="quick-link-item">
              <el-icon :size="40"><Tickets /></el-icon>
              <span>我的借阅</span>
            </div>
          </el-card>
          <el-card shadow="hover" @click="router.push('/my-reservations')">
            <div class="quick-link-item">
              <el-icon :size="40"><Clock /></el-icon>
              <span>我的预约</span>
            </div>
          </el-card>
          <el-card shadow="hover" @click="router.push('/profile')">
            <div class="quick-link-item">
              <el-icon :size="40"><User /></el-icon>
              <span>个人中心</span>
            </div>
          </el-card>
        </div>

        <!-- 热门图书 -->
        <div class="popular-books">
          <h3>📚 热门图书</h3>
          <el-row :gutter="20">
            <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="book in popularBooks" :key="book.id">
              <el-card shadow="hover" class="book-card" @click="viewBookDetail(book.id)">
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
                  <div class="book-stock">
                    <el-tag size="small" :type="book.availableStock > 0 ? 'success' : 'danger'">
                      {{ book.availableStock > 0 ? `可借 ${book.availableStock} 本` : '暂无库存' }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 统计信息 -->
        <div class="stats-section" v-if="userStore.userInfo">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-item">
                  <el-icon :size="50" color="#409eff"><Reading /></el-icon>
                  <div class="stat-content">
                    <div class="stat-value">{{ userStore.userInfo.currentBorrowCount }}</div>
                    <div class="stat-label">当前借阅</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-item">
                  <el-icon :size="50" color="#67c23a"><Tickets /></el-icon>
                  <div class="stat-content">
                    <div class="stat-value">{{ userStore.userInfo.availableBorrowCount }}</div>
                    <div class="stat-label">可借数量</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div class="stat-item">
                  <el-icon :size="50" color="#e6a23c"><Clock /></el-icon>
                  <div class="stat-content">
                    <div class="stat-value">{{ userStore.userInfo.maxBorrowCount }}</div>
                    <div class="stat-label">最大借阅</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, ArrowDown, Search, Reading, Tickets, Clock, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getPopularBooks } from '@/api/book'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const popularBooks = ref([])

// 获取热门图书
const fetchPopularBooks = async () => {
  try {
    const res = await getPopularBooks(8)
    popularBooks.value = res
  } catch (error) {
    console.error('获取热门图书失败:', error)
  }
}

// 搜索图书
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  router.push({
    path: '/books',
    query: { title: searchKeyword.value }
  })
}

// 查看图书详情
const viewBookDetail = (bookId) => {
  router.push({
    path: '/books',
    query: { bookId }
  })
}

// 下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
      break
  }
}

onMounted(() => {
  fetchPopularBooks()
})
</script>

<style scoped>
.home-container {
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
  padding: 30px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.search-section {
  margin-bottom: 30px;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.quick-link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.3s;
}

.quick-link-item:hover {
  transform: translateY(-5px);
}

.quick-link-item span {
  font-size: 16px;
  font-weight: 500;
}

.popular-books h3 {
  margin-bottom: 20px;
  color: #333;
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

.book-stock {
  display: flex;
  justify-content: flex-end;
}

.stats-section {
  margin-top: 40px;
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
</style>
