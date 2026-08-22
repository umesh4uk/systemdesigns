import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { ordersApi } from '@/api/orders'
import { useAuthStore } from '@/stores/authStore'

export const ORDER_KEYS = {
  all: ['orders'] as const,
  detail: (id: string) => ['orders', id] as const,
}

export function useOrders(page = 0) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  return useQuery({
    queryKey: [...ORDER_KEYS.all, page],
    queryFn: () => ordersApi.getOrders(page),
    enabled: isAuthenticated,
  })
}

export function useOrder(id: string) {
  return useQuery({
    queryKey: ORDER_KEYS.detail(id),
    queryFn: () => ordersApi.getOrder(id),
    enabled: !!id,
  })
}

export function usePlaceOrder() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ordersApi.placeOrder,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ORDER_KEYS.all })
      qc.invalidateQueries({ queryKey: ['cart'] })
      toast.success('Order placed successfully!')
    },
    onError: (err: any) =>
      toast.error(err?.response?.data?.message ?? 'Could not place order'),
  })
}

export function useCancelOrder() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) =>
      ordersApi.cancelOrder(id, reason),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ORDER_KEYS.detail(id) })
      qc.invalidateQueries({ queryKey: ORDER_KEYS.all })
      toast.success('Order cancelled')
    },
    onError: (err: any) =>
      toast.error(err?.response?.data?.message ?? 'Cannot cancel this order'),
  })
}
