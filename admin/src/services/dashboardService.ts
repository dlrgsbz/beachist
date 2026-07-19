import { EnrichedStationsOutput, enrichStations, mapStationInfo } from './utils'
import { StationInfo, StationInfoMap } from '../dtos'

import { ApiClient } from '../modules/data'
import moment from 'moment'

export class DashboardService {
  constructor(private apiClient: ApiClient) {}

  public async getStationWithInfo(): Promise<StationInfo[]> {
    const [stations, data] = await Promise.all([this.apiClient.fetchStations(), this.apiClient.fetchStationInfo()])
    const basicInfo = mapStationInfo(data)

    return addStationInfoToStations(stations, basicInfo)
  }

  public async getStationsWithCrew(date: moment.Moment): Promise<EnrichedStationsOutput> {
    const [stations, crews] = await Promise.all([this.apiClient.fetchStations(), this.apiClient.fetchCrews(date)])

    return enrichStations(stations, crews)
  }
}

const addStationInfoToStations = (stationInfo: StationInfo[], stationInfoMap: StationInfoMap) => {
  return stationInfo.map(station => ({ ...station, ...stationInfoMap[station.id] }))
}
