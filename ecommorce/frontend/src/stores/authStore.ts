import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Customer } from '@/types'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  customer: Customer | null
  isAuthenticated: boolean
  setTokens: (accessToken: string, refreshToken: string) => void
  setCustomer: (customer: Customer) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      customer: null,
      isAuthenticated: false,

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken, isAuthenticated: true }),

      setCustomer: (customer) => set({ customer }),

      logout: () =>
        set({ accessToken: null, refreshToken: null, customer: null, isAuthenticated: false }),
    }),
    {
      name: 'auth-storage',
      // Only persist tokens, not the full customer object (refetch on load)
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
)
