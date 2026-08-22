import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ShoppingCart, ChevronLeft, Minus, Plus } from 'lucide-react'
import { useProduct } from '@/hooks/useProducts'
import { useAddToCart } from '@/hooks/useCart'
import { useAuthStore } from '@/stores/authStore'
import { PageSpinner } from '@/components/ui/Spinner'

export function ProductDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { data: product, isLoading, error } = useProduct(id!)
  const addToCart = useAddToCart()
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const [quantity, setQuantity] = useState(1)
  const [selectedImage, setSelectedImage] = useState(0)

  if (isLoading) return <PageSpinner />
  if (error || !product) {
    return (
      <div className="text-center py-16">
        <p className="text-gray-500">Product not found.</p>
        <Link to="/products" className="btn-primary mt-4">Browse products</Link>
      </div>
    )
  }

  const primaryImage = product.images.find((i) => i.primary) ?? product.images[selectedImage]

  return (
    <div className="space-y-8">
      {/* Breadcrumb */}
      <nav aria-label="Breadcrumb">
        <ol className="flex items-center gap-2 text-sm text-gray-500">
          <li><Link to="/" className="hover:text-primary-600">Home</Link></li>
          <li>/</li>
          <li><Link to="/products" className="hover:text-primary-600">Products</Link></li>
          <li>/</li>
          <li className="text-gray-900 font-medium truncate max-w-xs">{product.name}</li>
        </ol>
      </nav>

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
        {/* Images */}
        <div className="space-y-4">
          <div className="aspect-square overflow-hidden rounded-xl bg-gray-100">
            {primaryImage ? (
              <img
                src={primaryImage.url}
                alt={primaryImage.altText ?? product.name}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex h-full items-center justify-center text-gray-300">
                <ShoppingCart className="h-16 w-16" />
              </div>
            )}
          </div>
          {product.images.length > 1 && (
            <div className="flex gap-2 overflow-x-auto pb-1">
              {product.images.map((img, i) => (
                <button
                  key={img.id}
                  onClick={() => setSelectedImage(i)}
                  className={`h-16 w-16 flex-shrink-0 overflow-hidden rounded-lg border-2 transition-colors
                    ${i === selectedImage ? 'border-primary-500' : 'border-transparent'}`}
                  aria-label={`Image ${i + 1}`}
                >
                  <img src={img.url} alt="" className="h-full w-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Details */}
        <div className="space-y-6">
          {product.categoryName && (
            <p className="text-sm font-medium uppercase tracking-wide text-primary-600">
              {product.categoryName}
            </p>
          )}
          <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>

          {product.brand && (
            <p className="text-gray-500">Brand: <span className="font-medium text-gray-700">{product.brand}</span></p>
          )}

          <p className="text-3xl font-bold text-gray-900">
            {product.currency} {product.basePrice.toFixed(2)}
          </p>

          {product.shortDescription && (
            <p className="text-gray-600">{product.shortDescription}</p>
          )}

          {/* Attributes */}
          {Object.keys(product.attributes).length > 0 && (
            <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
              {Object.entries(product.attributes).map(([key, value]) => (
                <div key={key}>
                  <dt className="font-medium capitalize text-gray-500">{key}</dt>
                  <dd className="text-gray-900">{value}</dd>
                </div>
              ))}
            </dl>
          )}

          {/* Add to cart */}
          {isAuthenticated ? (
            <div className="flex items-center gap-4">
              <div className="flex items-center rounded-md border border-gray-300">
                <button
                  onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                  className="px-3 py-2 text-gray-500 hover:text-gray-700"
                  aria-label="Decrease quantity"
                >
                  <Minus className="h-4 w-4" />
                </button>
                <span className="w-12 text-center font-medium">{quantity}</span>
                <button
                  onClick={() => setQuantity((q) => q + 1)}
                  className="px-3 py-2 text-gray-500 hover:text-gray-700"
                  aria-label="Increase quantity"
                >
                  <Plus className="h-4 w-4" />
                </button>
              </div>
              <button
                onClick={() => addToCart.mutate({ productId: product.id, quantity })}
                disabled={addToCart.isPending}
                className="btn-primary gap-2 flex-1"
              >
                <ShoppingCart className="h-4 w-4" />
                {addToCart.isPending ? 'Adding…' : 'Add to Cart'}
              </button>
            </div>
          ) : (
            <Link to="/login" className="btn-primary w-full justify-center">
              Log in to purchase
            </Link>
          )}

          {/* Full description */}
          {product.description && (
            <div className="border-t pt-6">
              <h2 className="font-semibold text-gray-900 mb-2">Description</h2>
              <p className="text-gray-600 whitespace-pre-line">{product.description}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
