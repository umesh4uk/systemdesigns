import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { cartApi } from '@/api/cart'
import { useCartStore } from '@/stores/cartStore'
import { useAuthStore } from '@/stores/authStore'

export const CART_KEY = ['cart'] as const

export function useCart() {
  const { setCart } = useCartStore()
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)

  return useQuery({
    queryKey: CART_KEY,
    queryFn: async () => {
      const cart = await cartApi.getCart()
      setCart(cart)
      return cart
    },
    enabled: isAuthenticated,
  })
}

export function useAddToCart() {
  const qc = useQueryClient()
  const { setCart } = useCartStore()

  return useMutation({
    mutationFn: ({ productId, quantity }: { productId: string; quantity: number }) =>
      cartApi.addItem(productId, quantity),
    onSuccess: (cart) => {
      setCart(cart)
      qc.setQueryData(CART_KEY, cart)
      toast.success('Added to cart')
    },
    onError: () => toast.error('Could not add item to cart'),
  })
}

export function useUpdateCartItem() {
  const qc = useQueryClient()
  const { setCart } = useCartStore()

  return useMutation({
    mutationFn: ({ productId, quantity }: { productId: string; quantity: number }) =>
      cartApi.updateQuantity(productId, quantity),
    onSuccess: (cart) => {
      setCart(cart)
      qc.setQueryData(CART_KEY, cart)
    },
    onError: () => toast.error('Could not update quantity'),
  })
}

export function useRemoveCartItem() {
  const qc = useQueryClient()
  const { setCart } = useCartStore()

  return useMutation({
    mutationFn: (productId: string) => cartApi.removeItem(productId),
    onSuccess: (cart) => {
      setCart(cart)
      qc.setQueryData(CART_KEY, cart)
      toast.success('Item removed')
    },
    onError: () => toast.error('Could not remove item'),
  })
}

export function useApplyCoupon() {
  const qc = useQueryClient()
  const { setCart } = useCartStore()

  return useMutation({
    mutationFn: (code: string) => cartApi.applyCoupon(code),
    onSuccess: (cart) => {
      setCart(cart)
      qc.setQueryData(CART_KEY, cart)
      toast.success('Coupon applied!')
    },
    onError: () => toast.error('Invalid or expired coupon'),
  })
}
