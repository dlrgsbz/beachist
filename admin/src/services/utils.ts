import { CrewInfo, StationInfo, StationInfoMap } from 'dtos'

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
  crews: Map<string, string>
}

export const enrichStations = (
  input: StationInfo[],
  stationsInfoMap: StationInfoMap,
  crewInfo: CrewInfo[],
): EnrichedStationsOutput => {
  const stationMap = new Map<string, StationInfo>()
  const crews = new Map<string, string>()
  const stations = input.map(station => {
    stationMap.set(station.id, station)
    const crew = crewInfo.find(val => val.station === station.id)
    if (crew) {
      crews.set(station.id, crew.crew)
    }
    return { ...station, ...stationsInfoMap[station.id] }
  })

  return { stations, stationMap, crews }
}
