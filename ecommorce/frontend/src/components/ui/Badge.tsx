import { clsx } from 'clsx'
import type { OrderStatus } from '@/types'

const STATUS_STYLES: Record<OrderStatus, string> = {
  CREATED:          'bg-gray-100 text-gray-800',
  PAYMENT_PENDING:  'bg-yellow-100 text-yellow-800',
  CONFIRMED:        'bg-blue-100 text-blue-800',
  PROCESSING:       'bg-indigo-100 text-indigo-800',
  SHIPPED:          'bg-purple-100 text-purple-800',
  DELIVERED:        'bg-green-100 text-green-800',
  CANCELLED:        'bg-red-100 text-red-800',
  PAYMENT_FAILED:   'bg-red-100 text-red-800',
  RETURN_REQUESTED: 'bg-orange-100 text-orange-800',
  RETURNED:         'bg-orange-100 text-orange-800',
  REFUNDED:         'bg-teal-100 text-teal-800',
}

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span className={clsx('badge', STATUS_STYLES[status])}>
      {status.replace(/_/g, ' ')}
    </span>
  )
}

export function ProductStatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    ACTIVE:   'bg-green-100 text-green-800',
    DRAFT:    'bg-gray-100 text-gray-800',
    ARCHIVED: 'bg-yellow-100 text-yellow-800',
    DELETED:  'bg-red-100 text-red-800',
  }
  return (
    <span className={clsx('badge', styles[status] ?? 'bg-gray-100 text-gray-800')}>
      {status}
    </span>
  )
}
