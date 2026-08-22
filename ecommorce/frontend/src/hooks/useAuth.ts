import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { authApi, type LoginRequest, type RegisterRequest } from '@/api/auth'
import { customersApi } from '@/api/customers'
import { useAuthStore } from '@/stores/authStore'

export function useCurrentUser() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const setCustomer = useAuthStore((s) => s.setCustomer)
  return useQuery({
    queryKey: ['me'],
    queryFn: async () => {
      const customer = await customersApi.getMe()
      setCustomer(customer)
      return customer
    },
    enabled: isAuthenticated,
    staleTime: 1000 * 60 * 5,
  })
}

export function useLogin() {
  const { setTokens } = useAuthStore()
  const navigate = useNavigate()
  const qc = useQueryClient()

  return useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: (tokens) => {
      setTokens(tokens.accessToken, tokens.refreshToken)
      qc.invalidateQueries({ queryKey: ['me'] })
      qc.invalidateQueries({ queryKey: ['cart'] })
      toast.success('Welcome back!')
      navigate('/')
    },
    onError: () => toast.error('Invalid email or password'),
  })
}

export function useRegister() {
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: () => {
      toast.success('Account created! Please log in.')
      navigate('/login')
    },
    onError: (err: any) =>
      toast.error(err?.response?.data?.message ?? 'Registration failed'),
  })
}

export function useLogout() {
  const { logout } = useAuthStore()
  const navigate = useNavigate()
  const qc = useQueryClient()

  return () => {
    logout()
    qc.clear()
    navigate('/login')
    toast.success('Logged out')
  }
}
