import { StationInfo, StationInfoMap } from 'dtos'

import { ApiStationInfo } from 'modules/data/dtos'
import moment from 'moment'

export const mapStationInfo = (data: Record<string, ApiStationInfo | null>): StationInfoMap =>
  Object.entries(data).reduce((carry, [key, data]) => {
    carry[key] = data
      ? {
          online: data.online,
          onlineStateSince: moment(data.date),
          appVersion: data.version,
          appVersionCode: data.versionCode,
        }
      : null
    return carry
  }, {} as StationInfoMap)

export interface EnrichedStationsOutput {
  stations: StationInfo[]
  stationMap: Map<string, StationInfo>
}

export const enrichStations = (input: StationInfo[], stationsInfoMap: StationInfoMap): EnrichedStationsOutput => {
  const stationMap = new Map<string, StationInfo>()
  const stations = input.map(station => {
    stationMap.set(station.id, station)
    return { ...station, ...stationsInfoMap[station.id] }
  })

  return { stations, stationMap }
}
