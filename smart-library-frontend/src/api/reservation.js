import request from '@/utils/request'

/**
 * 预约图书
 */
export function reserveBook(data) {
    return request({
        url: '/reservation',
        method: 'post',
        data
    })
}

/**
 * 取消预约
 */
export function cancelReservation(reservationId, data) {
    return request({
        url: `/reservation/${reservationId}/cancel`,
        method: 'post',
        data
    })
}

/**
 * 获取当前用户的预约记录
 */
export function getMyReservations(params) {
    return request({
        url: '/reservation/my-reservations',
        method: 'get',
        params
    })
}

/**
 * 获取图书的预约队列
 */
export function getBookReservationQueue(bookId, params) {
    return request({
        url: `/reservation/book/${bookId}/queue`,
        method: 'get',
        params
    })
}
