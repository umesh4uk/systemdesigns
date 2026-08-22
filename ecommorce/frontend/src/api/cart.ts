import { apiClient, unwrap } from '@/lib/apiClient'
import type { Cart } from '@/types'

export const cartApi = {
  getCart: () =>
    apiClient.get<{ success: boolean; data: Cart }>('/cart').then(unwrap),

  addItem: (productId: string, quantity: number) =>
    apiClient
      .post<{ success: boolean; data: Cart }>('/cart/items', { productId, quantity })
      .then(unwrap),

  updateQuantity: (productId: string, quantity: number) =>
    apiClient
      .put<{ success: boolean; data: Cart }>(`/cart/items/${productId}`, null, {
        params: { quantity },
      })
      .then(unwrap),

  removeItem: (productId: string) =>
    apiClient
      .delete<{ success: boolean; data: Cart }>(`/cart/items/${productId}`)
      .then(unwrap),

  applyCoupon: (code: string) =>
    apiClient
      .post<{ success: boolean; data: Cart }>('/cart/coupon', null, { params: { code } })
      .then(unwrap),

  removeCoupon: () =>
    apiClient
      .delete<{ success: boolean; data: Cart }>('/cart/coupon')
      .then(unwrap),

  clearCart: () => apiClient.delete('/cart'),
}
