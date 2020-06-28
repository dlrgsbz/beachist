import { configure } from 'mobx'
import AdminStore from 'store/stores/admin.store'
import NavigationStore from 'store/stores/navigation.store'

configure({ enforceActions: 'observed' })

class RootStore {
  public adminStore: AdminStore
  public navigationStore: NavigationStore

  constructor() {
    this.adminStore = new AdminStore()
    this.navigationStore = new NavigationStore()
  }
}

export default RootStore
