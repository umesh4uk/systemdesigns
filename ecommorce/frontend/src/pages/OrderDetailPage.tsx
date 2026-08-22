import { useParams, Link } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import { useOrder, useCancelOrder } from '@/hooks/useOrders'
import { OrderStatusBadge } from '@/components/ui/Badge'
import { PageSpinner } from '@/components/ui/Spinner'
import { format } from 'date-fns'

const CANCELLABLE = new Set(['CREATED', 'PAYMENT_PENDING', 'PAYMENT_FAILED', 'CONFIRMED', 'PROCESSING'])

export function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { data: order, isLoading } = useOrder(id!)
  const cancelOrder = useCancelOrder()

  if (isLoading) return <PageSpinner />
  if (!order) return <div className="text-center py-16 text-gray-500">Order not found.</div>

  const canCancel = CANCELLABLE.has(order.status)

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Back link */}
      <Link to="/orders" className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700">
        <ChevronLeft className="h-4 w-4" />Back to orders
      </Link>

      {/* Header */}
      <div className="card p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-xl font-bold text-gray-900">{order.orderNumber}</h1>
            <p className="text-sm text-gray-500 mt-1">
              Placed on {format(new Date(order.createdAt), 'PPP')}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <OrderStatusBadge status={order.status} />
            {canCancel && (
              <button
                onClick={() => cancelOrder.mutate({ id: order.id, reason: 'Customer request' })}
                disabled={cancelOrder.isPending}
                className="btn-danger text-xs"
              >
                Cancel order
              </button>
            )}
          </div>
        </div>

        {order.trackingNumber && (
          <div className="mt-4 rounded-lg bg-blue-50 px-4 py-3">
            <p className="text-sm font-medium text-blue-800">
              Tracking: <span className="font-mono">{order.trackingNumber}</span>
            </p>
          </div>
        )}

        {order.cancellationReason && (
          <div className="mt-4 rounded-lg bg-red-50 px-4 py-3">
            <p className="text-sm text-red-800">Reason: {order.cancellationReason}</p>
          </div>
        )}
      </div>

      {/* Items */}
      <div className="card p-6 space-y-4">
        <h2 className="font-semibold text-gray-900">Items</h2>
        {order.items.map((item) => (
          <div key={item.productId} className="flex justify-between gap-4 py-3 border-b last:border-0">
            <div>
              <p className="font-medium text-gray-900">{item.productName}</p>
              <p className="text-sm text-gray-500">SKU: {item.sku} · Qty: {item.quantity}</p>
            </div>
            <div className="text-right">
              <p className="font-medium">{item.currency} {item.lineTotal.toFixed(2)}</p>
              <p className="text-xs text-gray-500">{item.currency} {item.unitPrice.toFixed(2)} each</p>
            </div>
          </div>
        ))}
      </div>

      {/* Totals + Address */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
        {/* Totals */}
        <div className="card p-6 space-y-2 text-sm">
          <h2 className="font-semibold text-gray-900 mb-3">Payment Summary</h2>
          <div className="flex justify-between"><span className="text-gray-500">Subtotal</span><span>{order.currency} {order.subtotal.toFixed(2)}</span></div>
          {order.discount > 0 && <div className="flex justify-between text-green-600"><span>Discount</span><span>-{order.currency} {order.discount.toFixed(2)}</span></div>}
          <div className="flex justify-between"><span className="text-gray-500">Shipping</span><span>{order.currency} {order.shipping.toFixed(2)}</span></div>
          <div className="flex justify-between font-bold text-base border-t pt-2"><span>Total</span><span>{order.currency} {order.total.toFixed(2)}</span></div>
          {order.couponCode && <p className="text-xs text-gray-400">Coupon: {order.couponCode}</p>}
        </div>

        {/* Shipping address is in order data from server — show placeholder */}
        <div className="card p-6">
          <h2 className="font-semibold text-gray-900 mb-3">Need Help?</h2>
          <p className="text-sm text-gray-500">For returns and refunds, please contact our support team.</p>
          {order.status === 'DELIVERED' && (
            <Link to={`/orders/${order.id}/return`} className="btn-secondary mt-4 text-sm">
              Request Return
            </Link>
          )}
        </div>
      </div>
    </div>
  )
}
