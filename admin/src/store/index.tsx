import React, { ReactNode } from 'react'

import AdminStore from './stores/admin.store'
import { ApiClient } from '../modules/data'
import { AuthStore } from './stores/auth.store'
import DashboardStore from 'store/stores/dashboard.store'
import ItemsStore from './stores/items.store'
import RootStore from './stores/root.store'
import { useAuthService } from '../context/AuthServiceContext'

const StoreContext = React.createContext<RootStore | null>(null)

type StoreProviderProps = {
  children?: ReactNode
}

const StoreProvider = ({ children }: StoreProviderProps): React.JSX.Element => {
  const authService = useAuthService()
  const apiClient = new ApiClient(authService)
  const rootStore = new RootStore(apiClient)

  return <StoreContext value={rootStore}>{children}</StoreContext>
}

function useStore(): RootStore {
  const store = React.use(StoreContext)
  if (!store) {
    throw new Error('useStore: !store, did you forget StoreProvider?')
  }
  return store
}

export function useAdminStore(): AdminStore {
  return useStore().adminStore
}

export function useItemsStore(): ItemsStore {
  return useStore().itemsStore
}

export function useDashboardStore(): DashboardStore {
  return useStore().dashboardStore
}

export function useAuthStore(): AuthStore {
  return useStore().authStore
}

export default StoreProvider
