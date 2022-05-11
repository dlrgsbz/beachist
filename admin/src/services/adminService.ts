import moment from 'moment'
import { ApiClient } from 'modules/data'
import { ProvisioningRequest, ProvisioningRequestMap, StationInfo } from 'dtos'
import { enrichStations, mapStationInfo } from './utils'
import { ApiProvisioningRequest } from 'modules/data/dtos'

export class AdminService {
  constructor(private apiClient: ApiClient) {}

  public async getStationsWithInfo(): Promise<StationInfo[]> {
    const [stations, stationsInfo] = await Promise.all([
      this.apiClient.fetchStations(),
      this.apiClient.fetchStationInfo(),
    ])

    const stationsInfoMap = mapStationInfo(stationsInfo)

    return enrichStations(stations, stationsInfoMap).stations
  }

  public async getProvisioningRequests(): Promise<ProvisioningRequestMap> {
    const data = await this.apiClient.fetchProvisioningRequests()
    return Object.entries(data).reduce<ProvisioningRequestMap>(
      (carry, [id, value]) => {
        carry[id] = fromDtoToProvisioningRequest(value)
        return carry
      }, {})
  }

  public async createProvisioningRequest(stationId: string): Promise<ProvisioningRequest> {
    const data = await this.apiClient.createProvisioningRequest(stationId)
    return fromDtoToProvisioningRequest(data)
  }
}

const fromDtoToProvisioningRequest = (value: ApiProvisioningRequest): ProvisioningRequest => ({
  ...value,
  expiresAt: moment(value.expiresAt),
})
