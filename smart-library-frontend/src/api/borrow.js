import request from '@/utils/request'

/**
 * 借书
 */
export function borrowBook(data) {
    return request({
        url: '/borrow',
        method: 'post',
        data
    })
}

/**
 * 还书
 */
export function returnBook(data) {
    return request({
        url: '/borrow/return',
        method: 'post',
        data
    })
}

/**
 * 续借
 */
export function renewBook(borrowRecordId) {
    return request({
        url: `/borrow/${borrowRecordId}/renew`,
        method: 'post'
    })
}

/**
 * 获取当前用户的借阅记录
 */
export function getMyBorrowRecords(params) {
    return request({
        url: '/borrow/my-records',
        method: 'get',
        params
    })
}

/**
 * 获取当前借阅中的记录
 */
export function getCurrentBorrowRecords() {
    return request({
        url: '/borrow/my-current',
        method: 'get'
    })
}

/**
 * 获取所有逾期的借阅记录（管理员）
 */
export function getOverdueRecords() {
    return request({
        url: '/borrow/overdue',
        method: 'get'
    })
}
