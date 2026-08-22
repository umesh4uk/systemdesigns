// ── Auth / Customer ───────────────────────────────────────────
export interface Customer {
  id: string
  email: string
  firstName: string
  lastName: string
  phone: string | null
  status: 'PENDING_VERIFICATION' | 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED'
  createdAt: string
}

export interface Address {
  id: string
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

// ── Catalog ───────────────────────────────────────────────────
export interface Category {
  id: string
  name: string
  slug: string
  description?: string
  imageUrl?: string
  parentId?: string
  displayOrder: number
  active: boolean
  children: Category[]
}

export interface ProductSummary {
  id: string
  sku: string
  name: string
  shortDescription?: string
  brand?: string
  basePrice: number
  currency: string
  status: 'DRAFT' | 'ACTIVE' | 'ARCHIVED' | 'DELETED'
  primaryImageUrl?: string
  categoryName?: string
}

export interface ProductDetail extends ProductSummary {
  description?: string
  weightGrams?: number
  categoryId?: string
  images: ProductImage[]
  attributes: Record<string, string>
  createdAt: string
  updatedAt: string
}

export interface ProductImage {
  id: string
  url: string
  altText?: string
  displayOrder: number
  primary: boolean
}

// ── Cart ──────────────────────────────────────────────────────
export interface Cart {
  cartId: string
  customerId: string
  items: CartItem[]
  subtotal: number
  discountAmount: number
  total: number
  currency: string
  appliedCouponCode?: string
}

export interface CartItem {
  productId: string
  sku: string
  productName: string
  quantity: number
  unitPrice: number
  lineTotal: number
  imageUrl?: string
}

// ── Orders ────────────────────────────────────────────────────
export type OrderStatus =
  | 'CREATED'
  | 'PAYMENT_PENDING'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'PAYMENT_FAILED'
  | 'RETURN_REQUESTED'
  | 'RETURNED'
  | 'REFUNDED'

export interface Order {
  id: string
  orderNumber: string
  customerId: string
  status: OrderStatus
  items: OrderItem[]
  subtotal: number
  discount: number
  shipping: number
  total: number
  currency: string
  couponCode?: string
  trackingNumber?: string
  cancellationReason?: string
  createdAt: string
}

export interface OrderItem {
  productId: string
  sku: string
  productName: string
  quantity: number
  unitPrice: number
  lineTotal: number
  currency: string
}

// ── Payments ──────────────────────────────────────────────────
export interface Payment {
  id: string
  orderId: string
  customerId: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'REFUNDED' | 'PARTIALLY_REFUNDED'
  amount: number
  currency: string
  provider: string
  providerTransactionId?: string
  failureReason?: string
  refundedAmount: number
  createdAt: string
}

// ── Coupons ───────────────────────────────────────────────────
export interface Coupon {
  id: string
  code: string
  description?: string
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT'
  discountValue: number
  minimumOrderAmount?: number
  maximumDiscountAmount?: number
  validFrom: string
  validUntil?: string
  maxUsageCount: number
  usageCount: number
  active: boolean
}
