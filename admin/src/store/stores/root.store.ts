import { configure } from 'mobx'
import DashboardStore from 'store/stores/dashboard.store'
import NavigationStore from 'store/stores/navigation.store'
import { AuthStore } from './auth.store'
import { ApiClient } from 'modules/data'
import AdminStore from './admin.store'
import { AdminService, DashboardService } from 'services'

configure({ enforceActions: 'observed' })

class RootStore {
  public dashboardStore: DashboardStore
  public navigationStore: NavigationStore
  public authStore: AuthStore
  public adminStore: AdminStore

  constructor(private apiClient: ApiClient) {
    this.dashboardStore = new DashboardStore(this.apiClient, new DashboardService(this.apiClient))
    this.navigationStore = new NavigationStore()
    this.authStore = new AuthStore(this.apiClient)
    this.adminStore = new AdminStore(new AdminService(this.apiClient))
  }
}

export default RootStore
