import { useQuery, useInfiniteQuery } from '@tanstack/react-query'
import { productsApi, type ProductSearchParams } from '@/api/products'

export const PRODUCT_KEYS = {
  all: ['products'] as const,
  search: (params: ProductSearchParams) => ['products', 'search', params] as const,
  detail: (id: string) => ['products', id] as const,
  categories: ['categories'] as const,
}

export function useProducts(params: ProductSearchParams = {}) {
  return useQuery({
    queryKey: PRODUCT_KEYS.search(params),
    queryFn: () => productsApi.search(params),
  })
}

export function useProduct(id: string) {
  return useQuery({
    queryKey: PRODUCT_KEYS.detail(id),
    queryFn: () => productsApi.getById(id),
    enabled: !!id,
  })
}

export function useCategories() {
  return useQuery({
    queryKey: PRODUCT_KEYS.categories,
    queryFn: productsApi.getCategories,
    staleTime: 1000 * 60 * 10, // categories change rarely
  })
}
