import { create } from 'zustand'
import type { Cart } from '@/types'

interface CartState {
  cart: Cart | null
  isLoading: boolean
  setCart: (cart: Cart | null) => void
  setLoading: (loading: boolean) => void
  itemCount: () => number
}

export const useCartStore = create<CartState>()((set, get) => ({
  cart: null,
  isLoading: false,
  setCart: (cart) => set({ cart }),
  setLoading: (isLoading) => set({ isLoading }),
  itemCount: () =>
    get().cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0,
}))
