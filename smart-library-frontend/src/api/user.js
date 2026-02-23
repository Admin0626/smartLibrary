import request from '@/utils/request'

/**
 * 获取用户信息
 */
export function getUserInfo(userId) {
    return request({
        url: `/users/${userId}`,
        method: 'get'
    })
}

/**
 * 获取当前用户信息
 */
export function getCurrentProfile() {
    return request({
        url: '/users/profile',
        method: 'get'
    })
}

/**
 * 更新用户信息
 */
export function updateProfile(data) {
    return request({
        url: '/users/profile',
        method: 'put',
        data
    })
}

/**
 * 获取所有用户列表（管理员）
 */
export function getAllUsers(params) {
    return request({
        url: '/users',
        method: 'get',
        params
    })
}
