import type { Category } from '@/types'

interface ProductFiltersProps {
  categories: Category[]
  keyword: string
  categoryId: string
  minPrice: string
  maxPrice: string
  onKeywordChange: (v: string) => void
  onCategoryChange: (v: string) => void
  onMinPriceChange: (v: string) => void
  onMaxPriceChange: (v: string) => void
  onReset: () => void
}

export function ProductFilters({
  categories, keyword, categoryId, minPrice, maxPrice,
  onKeywordChange, onCategoryChange, onMinPriceChange, onMaxPriceChange, onReset,
}: ProductFiltersProps) {
  return (
    <aside className="space-y-6">
      {/* Search */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Search</label>
        <input
          type="search"
          value={keyword}
          onChange={(e) => onKeywordChange(e.target.value)}
          placeholder="Search products…"
          className="input"
        />
      </div>

      {/* Category */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
        <select
          value={categoryId}
          onChange={(e) => onCategoryChange(e.target.value)}
          className="input"
          aria-label="Filter by category"
        >
          <option value="">All categories</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>{cat.name}</option>
          ))}
        </select>
      </div>

      {/* Price range */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Price range</label>
        <div className="flex gap-2">
          <input
            type="number"
            min={0}
            value={minPrice}
            onChange={(e) => onMinPriceChange(e.target.value)}
            placeholder="Min"
            className="input"
          />
          <input
            type="number"
            min={0}
            value={maxPrice}
            onChange={(e) => onMaxPriceChange(e.target.value)}
            placeholder="Max"
            className="input"
          />
        </div>
      </div>

      <button onClick={onReset} className="btn-secondary w-full">Reset filters</button>
    </aside>
  )
}
