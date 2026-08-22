import { apiClient, unwrap, type TokenResponse } from '@/lib/apiClient'
import type { Customer } from '@/types'

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  phone?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export const authApi = {
  register: (data: RegisterRequest) =>
    apiClient.post<{ success: boolean; data: Customer }>('/auth/register', data).then(unwrap),

  login: (data: LoginRequest) =>
    apiClient.post<{ success: boolean; data: TokenResponse }>('/auth/login', data).then(unwrap),

  refresh: (refreshToken: string) =>
    apiClient
      .post<{ success: boolean; data: TokenResponse }>('/auth/refresh', null, {
        headers: { 'X-Refresh-Token': refreshToken },
      })
      .then(unwrap),
}
