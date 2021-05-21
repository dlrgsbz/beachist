import React, { ReactNode } from 'react'
import RootStore from './stores/root.store'
import AdminStore from 'store/stores/admin.store'
import NavigationStore from 'store/stores/navigation.store'
import { AuthStore } from './stores/auth.store'

const StoreContext = React.createContext<RootStore | null>(null)

type StoreProviderProps = {
  children?: ReactNode
}

const StoreProvider = ({ children }: StoreProviderProps): JSX.Element => {
  const rootStore = new RootStore()

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

export function useNavigationStore(): NavigationStore {
  return useStore().navigationStore
}

export function useAuthStore(): AuthStore {
  return useStore().authStore
}

export default StoreProvider
