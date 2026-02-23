import request from '@/utils/request'

/**
 * 创建图书（管理员）
 */
export function createBook(data) {
    return request({
        url: '/books',
        method: 'post',
        data
    })
}

/**
 * 获取图书详情
 */
export function getBookById(bookId) {
    return request({
        url: `/books/${bookId}`,
        method: 'get'
    })
}

/**
 * 根据ISBN获取图书
 */
export function getBookByIsbn(isbn) {
    return request({
        url: `/books/isbn/${isbn}`,
        method: 'get'
    })
}

/**
 * 更新图书信息（管理员）
 */
export function updateBook(bookId, data) {
    return request({
        url: `/books/${bookId}`,
        method: 'put',
        data
    })
}

/**
 * 删除图书（管理员）
 */
export function deleteBook(bookId) {
    return request({
        url: `/books/${bookId}`,
        method: 'delete'
    })
}

/**
 * 搜索图书
 */
export function searchBooks(params) {
    return request({
        url: '/books/search',
        method: 'get',
        params
    })
}

/**
 * 获取可借阅图书列表
 */
export function getAvailableBooks(params) {
    return request({
        url: '/books/available',
        method: 'get',
        params
    })
}

/**
 * 获取热门图书
 */
export function getPopularBooks(limit = 10) {
    return request({
        url: '/books/popular',
        method: 'get',
        params: { limit }
    })
}
