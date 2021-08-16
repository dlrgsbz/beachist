import { action, observable, runInAction } from 'mobx'
import moment, { Moment } from 'moment'
import {
  Entry,
  EventEntry,
  Field,
  NetworkEntry,
  NetworkSpecialEvent,
  SpecialEvent,
  SpecialEventType,
  StationInfo,
} from 'dtos'
import { AdminView, Color } from 'interfaces'
import {
  ApiClient,
  sendEventToWukos,
} from 'modules/data'

const AUTO_UPDATE_TIMEOUT = 5 * 60 * 1000

class AdminStore {
  @observable selectedDate: Moment = moment()
  @observable feldListe = new Map<string, string>()
  @observable felder = new Map<string, Field>()
  @observable firstAid = 0
  @observable search = 0
  @observable loading = false
  @observable stations: StationInfo[] = []
  @observable fields: Field[] = []
  @observable entries = new Map<string, Entry[]>()
  @observable crews = new Map<string, string>()
  @observable damages: SpecialEvent[] = []
  @observable specialEvents: SpecialEvent[] = []

  @observable autoUpdateEnabled: boolean = true
  @observable view: AdminView = AdminView.stations

  private timeout: number | undefined

  constructor(private apiClient: ApiClient) {
  }

  async reloadData(): Promise<void> {
    this.setLoading(true)
    // @ts-ignore
    const [networkEntries, events, stations, fields, networkSpecialEvents] = await Promise.all<
      NetworkEntry[],
      EventEntry,
      StationInfo[],
      Field[],
      NetworkSpecialEvent[]
    >([
      this.apiClient.fetchEntries(this.selectedDate),
      this.apiClient.fetchEvents(this.selectedDate),
      this.apiClient.fetchStations(),
      this.apiClient.fetchFields(),
      this.apiClient.fetchSpecialEvents(this.selectedDate),
    ])

    const stationMap = new Map<string, StationInfo>()
    stations.forEach(station => stationMap.set(station.id, station))
    const fieldMap = new Map<string, Field>()
    fields.forEach(field => fieldMap.set(field.id, field))

    const { entries, crews } = createEntryMap(networkEntries, stationMap, fieldMap)

    const specialEvents = createSpecialEventMap(networkSpecialEvents, stationMap)

    runInAction(() => {
      this.firstAid = events.firstAid
      this.search = events.search
      this.stations = stations
      this.fields = fields
      this.entries = entries
      this.crews = crews
      this.specialEvents = specialEvents.special
      this.damages = specialEvents.damage
      this.view = AdminView.stations
      this.setLoading(false)
    })

    if (this.autoUpdateEnabled) {
      this.timeout = window.setTimeout(() => this.reloadData(), AUTO_UPDATE_TIMEOUT)
    }
  }

  @action.bound
  changeSelectedDate(date: Moment | null): void {
    if (date) {
      this.selectedDate = date
      this.reloadData()
    }
  }

  @action
  setLoading(loading: boolean): void {
    this.loading = loading
  }

  color(id: string): Color {
    const entries = this.entries.get(id)

    if (!entries || entries.length === 0) {
      return Color.yellow
    }

    return entries.filter(e => !e.state).length === 0 ? Color.green : Color.red
  }

  stationEntries(id: string): Entry[] {
    return this.entries.get(id) || []
  }

  @action.bound
  showStationInfo() {
    this.view = AdminView.stations
  }

  @action.bound
  showDamages() {
    this.view = AdminView.damages
  }

  @action.bound
  showSpecialEvents() {
    this.view = AdminView.specialEvents
  }

  @action.bound
  toggleAutoUpdate(): void {
    this.autoUpdateEnabled = !this.autoUpdateEnabled

    if (this.autoUpdateEnabled) {
      this.timeout = window.setTimeout(() => this.reloadData(), AUTO_UPDATE_TIMEOUT)
    } else {
      window.clearTimeout(this.timeout)
      this.timeout = undefined
    }
  }

  sendEventToWukos(event: SpecialEvent): void {
    sendEventToWukos(event)
  }
}

function createEntryMap(
  entries: NetworkEntry[],
  stationMap: Map<string, StationInfo>,
  fieldMap: Map<string, Field>,
): { entries: Map<string, Entry[]>; crews: Map<string, string> } {
  const crews = new Map<string, string>()

  const theEntries: Entry[] = entries
    .flatMap(entry => {
      const station = stationMap.get(entry.station)
      const field = fieldMap.get(entry.field)
      if (!station || !field) {
        return []
      }
      return [{ ...entry, station, field }]
    })

  const entryMap = new Map<string, Entry[]>()
  theEntries.forEach(entry => {
    let stationEntries = entryMap.get(entry.station.id)
    if (!stationEntries) {
      stationEntries = []
    }
    if (entry.crew) {
      crews.set(entry.station.id, entry.crew)
    }
    stationEntries.push(entry)
    entryMap.set(entry.station.id, stationEntries)
  })

  return {
    entries: entryMap,
    crews,
  }
}

interface SpecialEventMap {
  special: SpecialEvent[]
  damage: SpecialEvent[]
}

function createSpecialEventMap(
  specialEvents: NetworkSpecialEvent[],
  stationMap: Map<string, StationInfo>,
): SpecialEventMap {
  const map: SpecialEventMap = { special: [], damage: [] }

  specialEvents.forEach(event => {
    const stationId = event.station
    const station = stationMap.get(stationId)
    if (!station) {
      return
    }

    const specialEvent = { ...event, station }
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

export default AdminStore
