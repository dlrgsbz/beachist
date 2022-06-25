import { EnrichedStationsOutput, enrichStations, mapStationInfo } from './utils'

import { ApiClient } from '../modules/data'
import { StationInfoMap } from '../dtos'
import moment from 'moment'

export class DashboardService {
  constructor(private apiClient: ApiClient) {}

  public async getStationInfo(): Promise<StationInfoMap> {
    const data = await this.apiClient.fetchStationInfo()
    return mapStationInfo(data)
  }

  public async getStationsAndInfo(date: moment.Moment): Promise<EnrichedStationsOutput> {
    const [stations, stationsInfo, crews] = await Promise.all([
      this.apiClient.fetchStations(),
      this.apiClient.fetchStationInfo(),
      this.apiClient.fetchCrews(date),
    ])

    const stationsInfoMap = mapStationInfo(stationsInfo)

    return enrichStations(stations, stationsInfoMap, crews)
  }
}
