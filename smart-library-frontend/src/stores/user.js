import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

    /**
     * 登录
     */
    async function login(loginForm) {
        try {
            const res = await loginApi(loginForm)
            token.value = res.token
            userInfo.value = {
                userId: res.userId,
                username: res.username,
                realName: res.realName,
                role: res.role
            }

            // 保存到localStorage
            localStorage.setItem('token', res.token)
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

            return res
        } catch (error) {
            throw error
        }
    }

    /**
     * 注册
     */
    async function register(registerForm) {
        try {
            const res = await registerApi(registerForm)
            return res
        } catch (error) {
            throw error
        }
    }

    /**
     * 获取当前用户信息
     */
    async function fetchUserInfo() {
        try {
            const res = await getCurrentUser()
            userInfo.value = {
                userId: res.id,
                username: res.username,
                realName: res.realName,
                role: res.role,
                status: res.status,
                maxBorrowCount: res.maxBorrowCount,
                currentBorrowCount: res.currentBorrowCount,
                availableBorrowCount: res.availableBorrowCount
            }
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
            return res
        } catch (error) {
            throw error
        }
    }

    /**
     * 登出
     */
    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    /**
     * 检查是否已登录
     */
    function isLoggedIn() {
        return !!token.value
    }

    /**
     * 检查是否是管理员
     */
    function isAdmin() {
        return userInfo.value?.role === 'ADMIN'
    }

    return {
        token,
        userInfo,
        login,
        register,
        fetchUserInfo,
        logout,
        isLoggedIn,
        isAdmin
    }
})
