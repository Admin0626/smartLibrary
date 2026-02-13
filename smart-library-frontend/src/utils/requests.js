const { default: axios } = require("axios");
const { ElMessage } = require("element-plus");

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

//请求拦截器
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

//响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        //根据响应码输出内容
        if (res.code === 200){
            return res
        }else{
            ElMessage.error(res.message||'请求失败！')
            return   Promise.reject(new Error(res.message||'请求失败！'))
        }
    },
    error => {
        ElMessage.error(error.message||'网络错误！')
        return Promise.reject(error)
    }
)

export default request
