import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ProductCard } from '@/components/products/ProductCard'
import type { ProductSummary } from '@/types'

// Mock the auth store so the card renders without authentication
vi.mock('@/stores/authStore', () => ({
  useAuthStore: (selector: any) =>
    selector({ isAuthenticated: false, customer: null }),
}))

vi.mock('@/hooks/useCart', () => ({
  useAddToCart: () => ({ mutate: vi.fn(), isPending: false }),
}))

const mockProduct: ProductSummary = {
  id: '1',
  sku: 'TEST-001',
  name: 'Test Widget',
  shortDescription: 'A great widget',
  brand: 'Acme',
  basePrice: 29.99,
  currency: 'USD',
  status: 'ACTIVE',
  primaryImageUrl: undefined,
  categoryName: 'Electronics',
}

function renderWithProviders(ui: React.ReactElement) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <BrowserRouter>{ui}</BrowserRouter>
    </QueryClientProvider>,
  )
}

describe('ProductCard', () => {
  it('renders product name and price', () => {
    renderWithProviders(<ProductCard product={mockProduct} />)
    expect(screen.getByText('Test Widget')).toBeInTheDocument()
    expect(screen.getByText(/29\.99/)).toBeInTheDocument()
  })

  it('renders category name', () => {
    renderWithProviders(<ProductCard product={mockProduct} />)
    expect(screen.getByText('Electronics')).toBeInTheDocument()
  })

  it('links to product detail page', () => {
    renderWithProviders(<ProductCard product={mockProduct} />)
    const link = screen.getByRole('link')
    expect(link).toHaveAttribute('href', '/products/1')
  })

  it('does not show add-to-cart when not authenticated', () => {
    renderWithProviders(<ProductCard product={mockProduct} />)
    expect(screen.queryByRole('button', { name: /add/i })).not.toBeInTheDocument()
  })
})
