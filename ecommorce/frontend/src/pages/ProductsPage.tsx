import { useState, useDeferredValue } from 'react'
import { useProducts, useCategories } from '@/hooks/useProducts'
import { ProductCard } from '@/components/products/ProductCard'
import { ProductFilters } from '@/components/products/ProductFilters'
import { PageSpinner } from '@/components/ui/Spinner'
import { Pagination } from '@/components/ui/Pagination'
import { EmptyState } from '@/components/ui/EmptyState'
import { ShoppingBag } from 'lucide-react'

export function ProductsPage() {
  const [keyword, setKeyword]     = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [minPrice, setMinPrice]   = useState('')
  const [maxPrice, setMaxPrice]   = useState('')
  const [page, setPage]           = useState(0)

  const deferredKeyword = useDeferredValue(keyword)
  const { data: categories = [] } = useCategories()

  const { data, isLoading } = useProducts({
    keyword: deferredKeyword || undefined,
    categoryId: categoryId || undefined,
    minPrice: minPrice ? Number(minPrice) : undefined,
    maxPrice: maxPrice ? Number(maxPrice) : undefined,
    page,
    size: 20,
  })

  function handleReset() {
    setKeyword('')
    setCategoryId('')
    setMinPrice('')
    setMaxPrice('')
    setPage(0)
  }

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Products</h1>

      <div className="flex flex-col gap-8 lg:flex-row">
        {/* Filters sidebar */}
        <div className="lg:w-64 flex-shrink-0">
          <ProductFilters
            categories={categories}
            keyword={keyword}
            categoryId={categoryId}
            minPrice={minPrice}
            maxPrice={maxPrice}
            onKeywordChange={(v) => { setKeyword(v); setPage(0) }}
            onCategoryChange={(v) => { setCategoryId(v); setPage(0) }}
            onMinPriceChange={(v) => { setMinPrice(v); setPage(0) }}
            onMaxPriceChange={(v) => { setMaxPrice(v); setPage(0) }}
            onReset={handleReset}
          />
        </div>

        {/* Product grid */}
        <div className="flex-1 min-w-0">
          {isLoading ? (
            <PageSpinner />
          ) : !data || data.totalElements === 0 ? (
            <EmptyState
              icon={<ShoppingBag className="h-12 w-12" />}
              title="No products found"
              description="Try adjusting your filters"
              action={<button onClick={handleReset} className="btn-primary">Reset filters</button>}
            />
          ) : (
            <>
              <p className="text-sm text-gray-500 mb-4">
                {data.totalElements} product{data.totalElements !== 1 ? 's' : ''} found
              </p>
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
                {data.content.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
              <Pagination
                page={page}
                totalPages={data.totalPages}
                onPageChange={setPage}
              />
            </>
          )}
        </div>
      </div>
    </div>
  )
}
