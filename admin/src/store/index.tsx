import React, { ReactNode } from 'react'

import AdminStore from './stores/admin.store'
import { ApiClient } from '../modules/data'
import { AuthStore } from './stores/auth.store'
import DashboardStore from 'store/stores/dashboard.store'
import RootStore from './stores/root.store'
import { useAuthService } from '../context/AuthServiceContext'

const StoreContext = React.createContext<RootStore | null>(null)

type StoreProviderProps = {
  children?: ReactNode
}

const StoreProvider = ({ children }: StoreProviderProps): JSX.Element => {
  const authService = useAuthService()
  const apiClient = new ApiClient(authService)
  const rootStore = new RootStore(apiClient)

  return <StoreContext.Provider value={rootStore}>{children}</StoreContext.Provider>
}

function useStore(): RootStore {
  const store = React.useContext(StoreContext)
  if (!store) {
    throw new Error('useStore: !store, did you forget StoreProvider?')
  }
  return store
}

export function useAdminStore(): AdminStore {
  return useStore().adminStore
}

export function useDashboardStore(): DashboardStore {
  return useStore().dashboardStore
}

export function useAuthStore(): AuthStore {
  return useStore().authStore
}

export default StoreProvider
