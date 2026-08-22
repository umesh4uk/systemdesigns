import { Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, User, LogOut, Package, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { useAuthStore } from '@/stores/authStore'
import { useCartStore } from '@/stores/cartStore'
import { useLogout } from '@/hooks/useAuth'

export function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const customer = useAuthStore((s) => s.customer)
  const itemCount = useCartStore((s) => s.itemCount())
  const logout = useLogout()

  return (
    <header className="sticky top-0 z-40 bg-white shadow-sm border-b border-gray-200">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">

          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 text-xl font-bold text-primary-600">
            <Package className="h-6 w-6" />
            ShopNow
          </Link>

          {/* Desktop nav */}
          <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-gray-600">
            <Link to="/products" className="hover:text-primary-600 transition-colors">Products</Link>
            <Link to="/categories" className="hover:text-primary-600 transition-colors">Categories</Link>
          </nav>

          {/* Desktop actions */}
          <div className="hidden md:flex items-center gap-3">
            {/* Cart */}
            <Link
              to="/cart"
              className="relative p-2 text-gray-600 hover:text-primary-600 transition-colors"
              aria-label={`Cart, ${itemCount} items`}
            >
              <ShoppingCart className="h-6 w-6" />
              {itemCount > 0 && (
                <span className="absolute -right-0.5 -top-0.5 flex h-5 w-5 items-center justify-center
                                 rounded-full bg-primary-600 text-xs font-bold text-white">
                  {itemCount > 99 ? '99+' : itemCount}
                </span>
              )}
            </Link>

            {isAuthenticated ? (
              <div className="flex items-center gap-2">
                <Link to="/account" className="btn-secondary gap-1.5">
                  <User className="h-4 w-4" />
                  {customer?.firstName ?? 'Account'}
                </Link>
                <button onClick={logout} className="btn-secondary p-2" aria-label="Logout">
                  <LogOut className="h-4 w-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login" className="btn-secondary">Log in</Link>
                <Link to="/register" className="btn-primary">Sign up</Link>
              </div>
            )}
          </div>

          {/* Mobile menu button */}
          <button
            className="md:hidden p-2 text-gray-600"
            onClick={() => setMobileOpen(!mobileOpen)}
            aria-label="Toggle menu"
          >
            {mobileOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>

        {/* Mobile menu */}
        {mobileOpen && (
          <div className="md:hidden border-t border-gray-100 py-4 space-y-2">
            <Link to="/products" className="block px-3 py-2 text-gray-700 hover:bg-gray-50 rounded-md"
              onClick={() => setMobileOpen(false)}>Products</Link>
            <Link to="/cart" className="block px-3 py-2 text-gray-700 hover:bg-gray-50 rounded-md"
              onClick={() => setMobileOpen(false)}>Cart ({itemCount})</Link>
            {isAuthenticated ? (
              <>
                <Link to="/account" className="block px-3 py-2 text-gray-700 hover:bg-gray-50 rounded-md"
                  onClick={() => setMobileOpen(false)}>My Account</Link>
                <Link to="/orders" className="block px-3 py-2 text-gray-700 hover:bg-gray-50 rounded-md"
                  onClick={() => setMobileOpen(false)}>My Orders</Link>
                <button onClick={() => { logout(); setMobileOpen(false) }}
                  className="block w-full text-left px-3 py-2 text-red-600 hover:bg-red-50 rounded-md">
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="block px-3 py-2 text-gray-700 hover:bg-gray-50 rounded-md"
                  onClick={() => setMobileOpen(false)}>Log in</Link>
                <Link to="/register" className="block px-3 py-2 text-primary-600 font-medium hover:bg-primary-50 rounded-md"
                  onClick={() => setMobileOpen(false)}>Sign up</Link>
              </>
            )}
          </div>
        )}
      </div>
    </header>
  )
}
