import axios from 'axios'

// 创建axios实例
const request = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        // 如果响应码不是200，视为错误
        if (res.code !== 200) {
            console.error('响应错误:', res.message)

            // 401: 未授权，跳转到登录页
            if (res.code === 401) {
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                window.location.href = '/login'
            }

            // 返回错误信息
            return Promise.reject(new Error(res.message || '请求失败'))
        }

        // 返回数据部分
        return res.data
    },
    error => {
        console.error('响应错误:', error)

        // 处理网络错误
        if (error.response) {
            const { status, data } = error.response

            switch (status) {
                case 401:
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    window.location.href = '/login'
                    break
                case 403:
                    error.message = '无权限访问'
                    break
                case 404:
                    error.message = '请求的资源不存在'
                    break
                case 500:
                    error.message = data.message || '服务器内部错误'
                    break
                default:
                    error.message = data.message || '请求失败'
            }
        } else if (error.code === 'ECONNABORTED') {
            error.message = '请求超时'
        } else {
            error.message = '网络连接失败'
        }

        return Promise.reject(error)
    }
)

export default request
