import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Package } from 'lucide-react'
import { useOrders } from '@/hooks/useOrders'
import { OrderStatusBadge } from '@/components/ui/Badge'
import { PageSpinner } from '@/components/ui/Spinner'
import { Pagination } from '@/components/ui/Pagination'
import { EmptyState } from '@/components/ui/EmptyState'
import { format } from 'date-fns'

export function OrdersPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useOrders(page)

  if (isLoading) return <PageSpinner />

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">My Orders</h1>

      {!data || data.totalElements === 0 ? (
        <EmptyState
          icon={<Package className="h-16 w-16" />}
          title="No orders yet"
          description="Place your first order to see it here."
          action={<Link to="/products" className="btn-primary">Start shopping</Link>}
        />
      ) : (
        <>
          <div className="space-y-4">
            {data.content.map((order) => (
              <Link
                key={order.id}
                to={`/orders/${order.id}`}
                className="card p-5 block hover:shadow-md transition-shadow"
              >
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div className="space-y-1">
                    <div className="flex items-center gap-3">
                      <span className="font-semibold text-gray-900">{order.orderNumber}</span>
                      <OrderStatusBadge status={order.status} />
                    </div>
                    <p className="text-sm text-gray-500">
                      {format(new Date(order.createdAt), 'PPP')}
                    </p>
                    <p className="text-sm text-gray-600">
                      {order.items.length} item{order.items.length !== 1 ? 's' : ''}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold text-gray-900">
                      {order.currency} {order.total.toFixed(2)}
                    </p>
                    {order.trackingNumber && (
                      <p className="text-xs text-gray-500 mt-1">
                        Tracking: {order.trackingNumber}
                      </p>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>

          <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
