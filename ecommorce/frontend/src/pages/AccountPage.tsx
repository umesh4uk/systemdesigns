import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { customersApi } from '@/api/customers'
import { useCurrentUser } from '@/hooks/useAuth'
import { PageSpinner } from '@/components/ui/Spinner'
import { Link } from 'react-router-dom'
import { User, MapPin, ShoppingBag, Lock } from 'lucide-react'

const profileSchema = z.object({
  firstName: z.string().min(1),
  lastName:  z.string().min(1),
  phone:     z.string().optional(),
})

export function AccountPage() {
  const { data: customer, isLoading } = useCurrentUser()
  const qc = useQueryClient()
  const [activeTab, setActiveTab] = useState<'profile' | 'orders'>('profile')

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(profileSchema),
    values: { firstName: customer?.firstName ?? '', lastName: customer?.lastName ?? '', phone: customer?.phone ?? '' },
  })

  const updateProfile = useMutation({
    mutationFn: customersApi.updateProfile,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['me'] }); toast.success('Profile updated') },
    onError: () => toast.error('Could not update profile'),
  })

  if (isLoading) return <PageSpinner />

  const TABS = [
    { id: 'profile', label: 'Profile', icon: User },
    { id: 'orders',  label: 'Orders',  icon: ShoppingBag },
  ] as const

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">My Account</h1>

      <div className="flex gap-2 border-b border-gray-200">
        {TABS.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            onClick={() => setActiveTab(id)}
            className={`flex items-center gap-2 px-4 py-2 text-sm font-medium border-b-2 transition-colors
              ${activeTab === id ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          >
            <Icon className="h-4 w-4" />
            {label}
          </button>
        ))}
      </div>

      {activeTab === 'profile' && (
        <div className="max-w-lg space-y-6">
          {/* Profile form */}
          <div className="card p-6">
            <h2 className="font-semibold mb-4">Personal Information</h2>
            <form onSubmit={handleSubmit((data) => updateProfile.mutate(data))} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">First name</label>
                  <input className="input" {...register('firstName')} />
                  {errors.firstName && <p className="text-xs text-red-600 mt-1">{errors.firstName.message}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Last name</label>
                  <input className="input" {...register('lastName')} />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input className="input bg-gray-50" value={customer?.email ?? ''} disabled readOnly />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                <input className="input" {...register('phone')} />
              </div>
              <button type="submit" disabled={updateProfile.isPending} className="btn-primary">
                {updateProfile.isPending ? 'Saving…' : 'Save changes'}
              </button>
            </form>
          </div>

          {/* Quick links */}
          <div className="grid grid-cols-2 gap-4">
            <Link to="/account/addresses"
              className="card p-4 flex items-center gap-3 hover:shadow-md transition-shadow">
              <MapPin className="h-5 w-5 text-primary-600" />
              <span className="font-medium text-sm">Manage Addresses</span>
            </Link>
            <Link to="/account/password"
              className="card p-4 flex items-center gap-3 hover:shadow-md transition-shadow">
              <Lock className="h-5 w-5 text-primary-600" />
              <span className="font-medium text-sm">Change Password</span>
            </Link>
          </div>
        </div>
      )}

      {activeTab === 'orders' && (
        <div className="text-center py-8">
          <Link to="/orders" className="btn-primary">View all orders</Link>
        </div>
      )}
    </div>
  )
}
