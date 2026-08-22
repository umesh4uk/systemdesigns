import axios, { type AxiosError, type AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/authStore'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export const apiClient = axios.create({
  baseURL: `${BASE_URL}/api/v1`,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

// Attach access token to every request
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // Propagate correlation ID if present
  const correlationId = crypto.randomUUID()
  config.headers['X-Correlation-Id'] = correlationId
  return config
})

// Handle 401 — try refresh, else logout
apiClient.interceptors.response.use(
  (res: AxiosResponse) => res,
  async (error: AxiosError) => {
    const original = error.config as (typeof error.config & { _retry?: boolean })
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true
      try {
        const refreshToken = useAuthStore.getState().refreshToken
        if (!refreshToken) throw new Error('No refresh token')
        const { data } = await axios.post(`${BASE_URL}/api/v1/auth/refresh`, null, {
          headers: { 'X-Refresh-Token': refreshToken },
        })
        const payload = data.data as TokenResponse
        useAuthStore.getState().setTokens(payload.accessToken, payload.refreshToken)
        original.headers!.Authorization = `Bearer ${payload.accessToken}`
        return apiClient(original)
      } catch {
        useAuthStore.getState().logout()
      }
    }
    return Promise.reject(error)
  },
)

export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  timestamp: string
  correlationId?: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface ErrorResponse {
  status: number
  error: string
  message: string
  path: string
  timestamp: string
  fieldErrors?: Record<string, string[]>
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

// Convenience unwrap helpers
export const unwrap = <T>(res: AxiosResponse<ApiResponse<T>>): T => res.data.data
export const unwrapPage = <T>(res: AxiosResponse<ApiResponse<PageResponse<T>>>): PageResponse<T> =>
  res.data.data
