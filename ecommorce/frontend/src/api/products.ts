import { apiClient, unwrap, unwrapPage } from '@/lib/apiClient'
import type { ProductDetail, ProductSummary, Category } from '@/types'

export interface ProductSearchParams {
  keyword?: string
  categoryId?: string
  brand?: string
  minPrice?: number
  maxPrice?: number
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

export const productsApi = {
  search: (params: ProductSearchParams = {}) =>
    apiClient
      .get('/products', { params: { size: 20, ...params } })
      .then(unwrapPage<ProductSummary>),

  getById: (id: string) =>
    apiClient
      .get<{ success: boolean; data: ProductDetail }>(`/products/${id}`)
      .then(unwrap),

  getBySku: (sku: string) =>
    apiClient
      .get<{ success: boolean; data: ProductDetail }>(`/products/sku/${sku}`)
      .then(unwrap),

  getCategories: () =>
    apiClient
      .get<{ success: boolean; data: Category[] }>('/categories')
      .then(unwrap),
}
