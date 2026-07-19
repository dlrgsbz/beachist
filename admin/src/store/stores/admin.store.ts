import { AdminService, DashboardService } from 'services'
import { AsyncState, Result, createAsyncState, runWithAsyncState } from 'lib'
import { ProvisioningRequest, ProvisioningRequestMap, StationInfo } from 'dtos'
import { action, makeObservable, observable } from 'mobx'

class AdminStore {
  @observable stationsState: AsyncState<StationInfo[]> = createAsyncState([])
  @observable provisionMapState: AsyncState<ProvisioningRequestMap> = createAsyncState({})
  @observable creatProvisioningState: AsyncState<ProvisioningRequest | undefined> = createAsyncState(undefined)

  constructor(
    private adminService: AdminService,
    private dashboardService: DashboardService,
  ) {
    makeObservable(this)
  }

  @action.bound
  async fetchData(): Promise<void> {
    await Promise.all([this.fetchStations(), this.fetchProvisioningRequests()])
  }

  @action.bound
  async fetchStations(): Promise<Result<Error, StationInfo[]>> {
    return runWithAsyncState(this.stationsState, () => this.dashboardService.getStationWithInfo())
  }

  @action.bound
  async fetchProvisioningRequests(): Promise<Result<Error, ProvisioningRequestMap>> {
    return runWithAsyncState(this.provisionMapState, () => this.adminService.getProvisioningRequests())
  }

  @action.bound
  async createProvisioningRequest(stationId: string): Promise<Result<Error, ProvisioningRequest | undefined>> {
    return runWithAsyncState(this.creatProvisioningState, () => this.adminService.createProvisioningRequest(stationId))
  }
}

export default AdminStore
