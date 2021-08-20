import { configure } from 'mobx'
import AdminStore from 'store/stores/admin.store'
import NavigationStore from 'store/stores/navigation.store'
import { AuthStore } from './auth.store'
import { ApiClient } from 'modules/data'

configure({ enforceActions: 'observed' })

class RootStore {
  public adminStore: AdminStore
  public navigationStore: NavigationStore
  public authStore: AuthStore

  constructor(private apiClient: ApiClient) {
    this.adminStore = new AdminStore(this.apiClient)
    this.navigationStore = new NavigationStore()
    this.authStore = new AuthStore(this.apiClient)
  }
}

export default RootStore
