import { Link } from 'react-router-dom'
import { ShoppingBag, Tag, Truck } from 'lucide-react'
import { useProducts } from '@/hooks/useProducts'
import { ProductCard } from '@/components/products/ProductCard'
import { PageSpinner } from '@/components/ui/Spinner'

const FEATURES = [
  { icon: ShoppingBag, title: 'Wide Selection', desc: 'Thousands of products across all categories' },
  { icon: Tag,         title: 'Best Prices',   desc: 'Competitive pricing with regular promotions' },
  { icon: Truck,       title: 'Fast Delivery',  desc: 'Quick shipping right to your door' },
]

export function HomePage() {
  const { data, isLoading } = useProducts({ size: 8 })

  return (
    <div className="space-y-16">
      {/* Hero */}
      <section className="rounded-2xl bg-gradient-to-r from-primary-600 to-primary-700 px-8 py-16 text-white text-center">
        <h1 className="text-4xl font-extrabold mb-4">Discover Amazing Products</h1>
        <p className="text-primary-100 text-lg mb-8 max-w-xl mx-auto">
          Shop the latest trends at unbeatable prices. Free shipping on orders over $50.
        </p>
        <Link to="/products" className="inline-flex items-center gap-2 rounded-lg bg-white
          text-primary-700 font-semibold px-6 py-3 hover:bg-primary-50 transition-colors">
          <ShoppingBag className="h-5 w-5" />
          Shop Now
        </Link>
      </section>

      {/* Feature cards */}
      <section>
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
          {FEATURES.map(({ icon: Icon, title, desc }) => (
            <div key={title} className="card p-6 text-center">
              <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center
                              rounded-full bg-primary-100 text-primary-600">
                <Icon className="h-6 w-6" />
              </div>
              <h3 className="font-semibold text-gray-900">{title}</h3>
              <p className="mt-1 text-sm text-gray-500">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Featured products */}
      <section>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Featured Products</h2>
          <Link to="/products" className="text-sm font-medium text-primary-600 hover:text-primary-700">
            View all →
          </Link>
        </div>

        {isLoading ? (
          <PageSpinner />
        ) : (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {data?.content.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
