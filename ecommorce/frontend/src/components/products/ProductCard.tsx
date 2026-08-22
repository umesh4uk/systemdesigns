import { Link } from 'react-router-dom'
import { ShoppingCart } from 'lucide-react'
import { useAddToCart } from '@/hooks/useCart'
import { useAuthStore } from '@/stores/authStore'
import type { ProductSummary } from '@/types'

interface ProductCardProps {
  product: ProductSummary
}

export function ProductCard({ product }: ProductCardProps) {
  const addToCart = useAddToCart()
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)

  function handleAddToCart(e: React.MouseEvent) {
    e.preventDefault()
    e.stopPropagation()
    if (!isAuthenticated) return
    addToCart.mutate({ productId: product.id, quantity: 1 })
  }

  return (
    <Link
      to={`/products/${product.id}`}
      className="card group flex flex-col overflow-hidden hover:shadow-md transition-shadow"
    >
      {/* Image */}
      <div className="relative aspect-square overflow-hidden bg-gray-100">
        {product.primaryImageUrl ? (
          <img
            src={product.primaryImageUrl}
            alt={product.name}
            loading="lazy"
            className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-gray-300">
            <ShoppingCart className="h-12 w-12" />
          </div>
        )}
      </div>

      {/* Body */}
      <div className="flex flex-col flex-1 p-4 gap-2">
        {product.categoryName && (
          <p className="text-xs font-medium uppercase tracking-wide text-gray-500">
            {product.categoryName}
          </p>
        )}
        <h3 className="font-semibold text-gray-900 line-clamp-2 group-hover:text-primary-600 transition-colors">
          {product.name}
        </h3>
        {product.shortDescription && (
          <p className="text-sm text-gray-500 line-clamp-2">{product.shortDescription}</p>
        )}

        <div className="mt-auto flex items-center justify-between pt-2">
          <p className="text-lg font-bold text-gray-900">
            {product.currency} {product.basePrice.toFixed(2)}
          </p>

          {isAuthenticated && (
            <button
              onClick={handleAddToCart}
              disabled={addToCart.isPending}
              className="btn-primary gap-1.5 text-xs"
              aria-label={`Add ${product.name} to cart`}
            >
              <ShoppingCart className="h-3.5 w-3.5" />
              Add
            </button>
          )}
        </div>
      </div>
    </Link>
  )
}
