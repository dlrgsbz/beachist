import { configure } from 'mobx'
import AdminStore from 'store/stores/admin.store'
import NavigationStore from 'store/stores/navigation.store'
import { AuthStore } from './auth.store'

configure({ enforceActions: 'observed' })

class RootStore {
  public adminStore: AdminStore
  public navigationStore: NavigationStore
  public authStore: AuthStore

  constructor() {
    this.adminStore = new AdminStore()
    this.navigationStore = new NavigationStore()
    this.authStore = new AuthStore()
  }
}

export default RootStore
