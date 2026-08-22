import { apiClient, unwrap, unwrapPage } from '@/lib/apiClient'
import type { Order, Payment } from '@/types'

export const ordersApi = {
  placeOrder: (data: { shippingAddressId: string; billingAddressId: string; couponCode?: string }) =>
    apiClient
      .post<{ success: boolean; data: Order }>('/orders', data)
      .then(unwrap),

  getOrders: (page = 0, size = 10) =>
    apiClient
      .get('/orders', { params: { page, size } })
      .then(unwrapPage<Order>),

  getOrder: (id: string) =>
    apiClient
      .get<{ success: boolean; data: Order }>(`/orders/${id}`)
      .then(unwrap),

  cancelOrder: (id: string, reason?: string) =>
    apiClient
      .post<{ success: boolean; data: Order }>(`/orders/${id}/cancel`, null, {
        params: { reason },
      })
      .then(unwrap),

  requestReturn: (id: string) =>
    apiClient
      .post<{ success: boolean; data: Order }>(`/orders/${id}/return`)
      .then(unwrap),
}

export const paymentsApi = {
  initiatePayment: (data: { orderId: string; paymentMethodToken: string; idempotencyKey: string }) =>
    apiClient
      .post<{ success: boolean; data: Payment }>('/payments', data)
      .then(unwrap),

  getPayment: (id: string) =>
    apiClient
      .get<{ success: boolean; data: Payment }>(`/payments/${id}`)
      .then(unwrap),
}
