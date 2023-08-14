import { AdminFieldService, AdminService, DashboardService } from 'services'

import { AdminFieldStore } from './adminFieldStore'
import AdminStore from './admin.store'
import { ApiClientImpl } from 'modules/data'
import { AuthStore } from './auth.store'
import DashboardStore from 'store/stores/dashboard.store'
import { configure } from 'mobx'

configure({ enforceActions: 'observed' })

class RootStore {
  public dashboardStore: DashboardStore
  public authStore: AuthStore
  public adminStore: AdminStore
  public readonly adminFieldStore: AdminFieldStore

  constructor(private apiClient: ApiClientImpl) {
    this.dashboardStore = new DashboardStore(this.apiClient, new DashboardService(this.apiClient))
    this.authStore = new AuthStore(this.apiClient)
    this.adminStore = new AdminStore(new AdminService(this.apiClient))
    this.adminFieldStore = new AdminFieldStore(new AdminFieldService(this.apiClient))
  }
}

export default RootStore
