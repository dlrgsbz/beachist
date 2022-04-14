import { AdminService, DashboardService } from 'services'

import AdminStore from './admin.store'
import { ApiClient } from 'modules/data'
import { AuthStore } from './auth.store'
import DashboardStore from 'store/stores/dashboard.store'
import { configure } from 'mobx'

configure({ enforceActions: 'observed' })

class RootStore {
  public dashboardStore: DashboardStore
  public authStore: AuthStore
  public adminStore: AdminStore

  constructor(private apiClient: ApiClient) {
    this.dashboardStore = new DashboardStore(this.apiClient, new DashboardService(this.apiClient))
    this.authStore = new AuthStore(this.apiClient)
    this.adminStore = new AdminStore(new AdminService(this.apiClient))
  }
}

export default RootStore
