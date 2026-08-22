import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Trash2, Minus, Plus, Tag, ShoppingBag } from 'lucide-react'
import { useCart, useUpdateCartItem, useRemoveCartItem, useApplyCoupon } from '@/hooks/useCart'
import { PageSpinner } from '@/components/ui/Spinner'
import { EmptyState } from '@/components/ui/EmptyState'

export function CartPage() {
  const { data: cart, isLoading } = useCart()
  const updateItem  = useUpdateCartItem()
  const removeItem  = useRemoveCartItem()
  const applyCoupon = useApplyCoupon()
  const [couponInput, setCouponInput] = useState('')

  if (isLoading) return <PageSpinner />

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">Shopping Cart</h1>

      {!cart || cart.items.length === 0 ? (
        <EmptyState
          icon={<ShoppingBag className="h-16 w-16" />}
          title="Your cart is empty"
          description="Browse our products and add something you love."
          action={<Link to="/products" className="btn-primary">Shop now</Link>}
        />
      ) : (
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
          {/* Items */}
          <div className="lg:col-span-2 space-y-4">
            {cart.items.map((item) => (
              <div key={item.productId} className="card flex gap-4 p-4">
                {item.imageUrl ? (
                  <img src={item.imageUrl} alt={item.productName}
                    className="h-20 w-20 flex-shrink-0 rounded-lg object-cover bg-gray-100" />
                ) : (
                  <div className="h-20 w-20 flex-shrink-0 rounded-lg bg-gray-100" />
                )}

                <div className="flex flex-1 flex-col gap-2 min-w-0">
                  <h3 className="font-semibold text-gray-900 truncate">{item.productName}</h3>
                  <p className="text-sm text-gray-500">SKU: {item.sku}</p>
                  <p className="font-medium text-gray-900">
                    ${item.unitPrice.toFixed(2)} each
                  </p>

                  <div className="flex items-center justify-between">
                    {/* Quantity control */}
                    <div className="flex items-center rounded border border-gray-300">
                      <button
                        onClick={() => updateItem.mutate({ productId: item.productId, quantity: item.quantity - 1 })}
                        disabled={item.quantity <= 1}
                        className="px-2 py-1 text-gray-500 hover:text-gray-700 disabled:opacity-40"
                        aria-label="Decrease"
                      >
                        <Minus className="h-3.5 w-3.5" />
                      </button>
                      <span className="w-10 text-center text-sm font-medium">{item.quantity}</span>
                      <button
                        onClick={() => updateItem.mutate({ productId: item.productId, quantity: item.quantity + 1 })}
                        className="px-2 py-1 text-gray-500 hover:text-gray-700"
                        aria-label="Increase"
                      >
                        <Plus className="h-3.5 w-3.5" />
                      </button>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-semibold">${item.lineTotal.toFixed(2)}</span>
                      <button
                        onClick={() => removeItem.mutate(item.productId)}
                        className="text-red-500 hover:text-red-700"
                        aria-label="Remove item"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Summary */}
          <div className="space-y-4">
            <div className="card p-6 space-y-4">
              <h2 className="font-semibold text-lg text-gray-900">Order Summary</h2>

              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">Subtotal</span>
                  <span>${cart.subtotal.toFixed(2)}</span>
                </div>
                {cart.discountAmount > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Coupon ({cart.appliedCouponCode})</span>
                    <span>-${cart.discountAmount.toFixed(2)}</span>
                  </div>
                )}
                <div className="border-t pt-2 flex justify-between font-bold text-base">
                  <span>Total</span>
                  <span>{cart.currency} {cart.total.toFixed(2)}</span>
                </div>
              </div>

              {/* Coupon */}
              {!cart.appliedCouponCode && (
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={couponInput}
                    onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
                    placeholder="Coupon code"
                    className="input flex-1"
                  />
                  <button
                    onClick={() => { applyCoupon.mutate(couponInput); setCouponInput('') }}
                    disabled={!couponInput || applyCoupon.isPending}
                    className="btn-secondary gap-1"
                  >
                    <Tag className="h-4 w-4" />
                    Apply
                  </button>
                </div>
              )}

              <Link
                to="/checkout"
                className="btn-primary w-full justify-center text-base py-3"
              >
                Checkout
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
