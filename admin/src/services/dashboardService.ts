import { EnrichedStationsOutput, enrichStations, mapStationInfo } from './utils'

import { ApiClient } from '../modules/data'
import { StationInfoMap } from '../dtos'

export class DashboardService {
  constructor(private apiClient: ApiClient) {}

  public async getStationInfo(): Promise<StationInfoMap> {
    const data = await this.apiClient.fetchStationInfo()
    return mapStationInfo(data)
  }

  public async getStationsAndInfo(): Promise<EnrichedStationsOutput> {
    const [stations, stationsInfo] = await Promise.all([
      this.apiClient.fetchStations(),
      this.apiClient.fetchStationInfo(),
    ])

    const stationsInfoMap = mapStationInfo(stationsInfo)

    return enrichStations(stations, stationsInfoMap)
  }
}
