import { apiClient, unwrap } from '@/lib/apiClient'
import type { Customer, Address } from '@/types'

export interface AddressRequest {
  addressLine1: string
  addressLine2?: string
  city: string
  state?: string
  postalCode: string
  country: string
  addressType: 'SHIPPING' | 'BILLING' | 'BOTH'
  defaultAddress: boolean
  label?: string
}

export const customersApi = {
  getMe: () =>
    apiClient.get<{ success: boolean; data: Customer }>('/customers/me').then(unwrap),

  updateProfile: (data: { firstName: string; lastName: string; phone?: string }) =>
    apiClient.put<{ success: boolean; data: Customer }>('/customers/me', data).then(unwrap),

  changePassword: (data: { currentPassword: string; newPassword: string }) =>
    apiClient.patch('/customers/me/password', data),

  getAddresses: () =>
    apiClient
      .get<{ success: boolean; data: Address[] }>('/customers/me/addresses')
      .then(unwrap),

  addAddress: (data: AddressRequest) =>
    apiClient
      .post<{ success: boolean; data: Address }>('/customers/me/addresses', data)
      .then(unwrap),

  updateAddress: (id: string, data: AddressRequest) =>
    apiClient
      .put<{ success: boolean; data: Address }>(`/customers/me/addresses/${id}`, data)
      .then(unwrap),

  setDefaultAddress: (id: string) =>
    apiClient.patch(`/customers/me/addresses/${id}/default`),

  removeAddress: (id: string) =>
    apiClient.delete(`/customers/me/addresses/${id}`),
}
