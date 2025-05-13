import { AdminService, DashboardService } from 'services'

import AdminStore from './admin.store'
import { ApiClient } from 'modules/data'
import { AuthStore } from './auth.store'
import DashboardStore from 'store/stores/dashboard.store'
import { configure } from 'mobx'
import { MqttService } from '../../services/mqttService'
import MqttStore from './mqtt.store'

configure({ enforceActions: 'observed' })

class RootStore {
  public dashboardStore: DashboardStore
  public authStore: AuthStore
  public adminStore: AdminStore
  public mqttStore: MqttStore

  constructor(private apiClient: ApiClient) {
    this.dashboardStore = new DashboardStore(this.apiClient, new DashboardService(this.apiClient))
    this.authStore = new AuthStore(this.apiClient)
    this.adminStore = new AdminStore(new AdminService(this.apiClient))
    this.mqttStore = new MqttStore(new MqttService(this.apiClient))
  }
}

export default RootStore
