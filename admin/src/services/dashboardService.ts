import { EnrichedStationsOutput, enrichStations, mapStationInfo } from './utils'

import { ApiClient } from '../modules/data'
import {
  CrewInfo,
  Entry,
  EventEntry,
  Field,
  NetworkEntry,
  NetworkSpecialEvent,
  SpecialEventMap,
  SpecialEventType,
  StationInfo,
  StationInfoMap,
} from '../dtos'
import moment from 'moment'

export class DashboardService {
  constructor(private apiClient: ApiClient) {}

  public async getStationInfo(): Promise<StationInfoMap> {
    const data = await this.apiClient.fetchStationInfo()
    return mapStationInfo(data)
  }

  public async getCrews(date: moment.Moment): Promise<CrewInfo[]> {
    return this.apiClient.fetchCrews(date)
  }

  public async getStations(): Promise<StationInfo[]> {
    return this.apiClient.fetchStations()
  }

  public async getCrewInfo(date: moment.Moment): Promise<Map<string, string>> {
    const crews = await this.apiClient.fetchCrews(date)

    return mapCrewInfo(crews)
  }

  public async getEntries(date: moment.Moment): Promise<Map<string, Entry[]>> {
    const [fields, networkEntries] = await Promise.all([
      this.apiClient.fetchFields(),
      this.apiClient.fetchEntries(date),
    ])

    const fieldMap = new Map<string, Field>()
    fields.forEach(field => fieldMap.set(field.id, field))

    return createEntryMap(networkEntries, fieldMap)
  }

  public async getEvents(date: moment.Moment): Promise<EventEntry> {
    return this.apiClient.fetchEvents(date)
  }

  public async getStationsAndInfo(date: moment.Moment): Promise<EnrichedStationsOutput> {
    const [stations, crews] = await Promise.all([
      this.apiClient.fetchStations(),
      // this.apiClient.fetchStationInfo(),
      this.apiClient.fetchCrews(date),
    ])

    // const stationsInfoMap = mapStationInfo(stationsInfo)

    return enrichStations(stations, crews)
  }

  async getSpecialEvents(date: moment.Moment): Promise<SpecialEventMap> {
    const networkSpecialEvents = await this.apiClient.fetchSpecialEvents(date)

    return createSpecialEventMap(networkSpecialEvents)
  }
}

const mapCrewInfo = (data: CrewInfo[]) => {
  const crews = new Map<string, string>()
  data.forEach(d => {
    crews.set(d.stationId, d.crew)
  })

  return crews
}

function createEntryMap(
  entries: NetworkEntry[],
  fieldMap: Map<string, Field>,
): Map<string, Entry[]> {
  const theEntries: Entry[] = entries.flatMap(entry => {
    const field = fieldMap.get(entry.field)
    if (!field) {
      return []
    }
    return [{ ...entry, field }]
  })

  const entryMap = new Map<string, Entry[]>()
  theEntries.forEach(entry => {
    let stationEntries = entryMap.get(entry.station)
    if (!stationEntries) {
      stationEntries = []
    }

    stationEntries.push(entry)
    entryMap.set(entry.station, stationEntries)
  })

  return entryMap
}

function createSpecialEventMap(
  specialEvents: NetworkSpecialEvent[],
): SpecialEventMap {
  const map: SpecialEventMap = { special: [], damage: [] }

  specialEvents.forEach(event => {
    const stationId = event.station

    const specialEvent = { ...event, station: stationId }
    switch (event.type) {
      case SpecialEventType.damage:
        map.damage.push(specialEvent)
        break
      case SpecialEventType.event:
        map.special.push(specialEvent)
        break
    }
  })

  return map
}

