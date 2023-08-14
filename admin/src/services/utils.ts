import { BaseStationInfo, CrewInfo, StationInfo, StationInfoMap } from 'dtos'

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
  stationMap: Map<string, BaseStationInfo>
  crews: Map<string, string>
}

export const enrichStations = (
  input: BaseStationInfo[],
  stationsInfoMap: StationInfoMap,
  crewInfo: CrewInfo[],
): EnrichedStationsOutput => {
  const stationMap = new Map<string, BaseStationInfo>()
  const crews = new Map<string, string>()
  const stations = input.map(station => {
    stationMap.set(station.id, station)
    const crew = crewInfo.find(val => val.station === station.id)
    if (crew) {
      crews.set(station.id, crew.crew)
    }
    return { ...station, ...stationsInfoMap[station.id], appVersion: stationsInfoMap[station.id]?.appVersion ?? '0.0' }
  })

  return { stations, stationMap, crews }
}
