import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { customersApi } from '@/api/customers'
import { cartApi } from '@/api/cart'
import { usePlaceOrder } from '@/hooks/useOrders'
import { PageSpinner } from '@/components/ui/Spinner'

export function CheckoutPage() {
  const navigate = useNavigate()
  const placeOrder = usePlaceOrder()

  const { data: addresses = [], isLoading: addrLoading } = useQuery({
    queryKey: ['addresses'],
    queryFn: customersApi.getAddresses,
  })

  const { data: cart, isLoading: cartLoading } = useQuery({
    queryKey: ['cart'],
    queryFn: cartApi.getCart,
  })

  const [shippingId, setShippingId] = useState('')
  const [billingId,  setBillingId]  = useState('')

  if (addrLoading || cartLoading) return <PageSpinner />

  if (!cart || cart.items.length === 0) {
    return (
      <div className="text-center py-16">
        <p className="text-gray-500 mb-4">Your cart is empty.</p>
        <Link to="/products" className="btn-primary">Shop now</Link>
      </div>
    )
  }

  const shippingAddresses = addresses.filter((a) => a.addressType === 'SHIPPING' || a.addressType === 'BOTH')
  const billingAddresses  = addresses.filter((a) => a.addressType === 'BILLING'  || a.addressType === 'BOTH')

  async function handlePlaceOrder() {
    if (!shippingId || !billingId) return
    const order = await placeOrder.mutateAsync({
      shippingAddressId: shippingId,
      billingAddressId:  billingId,
      couponCode: cart?.appliedCouponCode,
    })
    navigate(`/orders/${order.id}`)
  }

  return (
    <div className="max-w-2xl mx-auto space-y-8">
      <h1 className="text-3xl font-bold text-gray-900">Checkout</h1>

      {addresses.length === 0 ? (
        <div className="card p-6 text-center space-y-3">
          <p className="text-gray-600">You need to add a delivery address first.</p>
          <Link to="/account/addresses" className="btn-primary">Add address</Link>
        </div>
      ) : (
        <>
          {/* Shipping address */}
          <div className="card p-6 space-y-4">
            <h2 className="font-semibold text-gray-900">Shipping Address</h2>
            <div className="space-y-2">
              {shippingAddresses.map((addr) => (
                <label key={addr.id}
                  className={`flex items-start gap-3 rounded-lg border-2 p-4 cursor-pointer transition-colors
                    ${shippingId === addr.id ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:border-gray-300'}`}>
                  <input type="radio" name="shipping" value={addr.id}
                    checked={shippingId === addr.id}
                    onChange={() => setShippingId(addr.id)}
                    className="mt-0.5 accent-primary-600" />
                  <div className="text-sm">
                    <p className="font-medium">{addr.label ?? addr.addressType}</p>
                    <p className="text-gray-600">{addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}</p>
                    <p className="text-gray-600">{addr.city}, {addr.state} {addr.postalCode}, {addr.country}</p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {/* Billing address */}
          <div className="card p-6 space-y-4">
            <h2 className="font-semibold text-gray-900">Billing Address</h2>
            <div className="space-y-2">
              {billingAddresses.map((addr) => (
                <label key={addr.id}
                  className={`flex items-start gap-3 rounded-lg border-2 p-4 cursor-pointer transition-colors
                    ${billingId === addr.id ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:border-gray-300'}`}>
                  <input type="radio" name="billing" value={addr.id}
                    checked={billingId === addr.id}
                    onChange={() => setBillingId(addr.id)}
                    className="mt-0.5 accent-primary-600" />
                  <div className="text-sm">
                    <p className="font-medium">{addr.label ?? addr.addressType}</p>
                    <p className="text-gray-600">{addr.addressLine1}</p>
                    <p className="text-gray-600">{addr.city}, {addr.state} {addr.postalCode}, {addr.country}</p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {/* Summary */}
          <div className="card p-6 space-y-4">
            <h2 className="font-semibold text-gray-900">Order Summary</h2>
            <div className="space-y-2 text-sm">
              {cart.items.map((item) => (
                <div key={item.productId} className="flex justify-between">
                  <span className="text-gray-600">{item.productName} × {item.quantity}</span>
                  <span>${item.lineTotal.toFixed(2)}</span>
                </div>
              ))}
              {cart.discountAmount > 0 && (
                <div className="flex justify-between text-green-600">
                  <span>Discount</span><span>-${cart.discountAmount.toFixed(2)}</span>
                </div>
              )}
              <div className="border-t pt-2 flex justify-between font-bold">
                <span>Total</span>
                <span>{cart.currency} {cart.total.toFixed(2)}</span>
              </div>
            </div>

            <button
              onClick={handlePlaceOrder}
              disabled={!shippingId || !billingId || placeOrder.isPending}
              className="btn-primary w-full justify-center text-base py-3"
            >
              {placeOrder.isPending ? 'Placing order…' : 'Place Order'}
            </button>
          </div>
        </>
      )}
    </div>
  )
}
