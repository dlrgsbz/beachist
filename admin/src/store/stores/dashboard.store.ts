import { action, observable, runInAction } from 'mobx'
import moment, { Moment } from 'moment'
import {
  Entry,
  Field,
  NetworkEntry,
  NetworkSpecialEvent,
  SpecialEvent,
  SpecialEventType,
  StationInfo,
} from 'dtos'
import { AdminView, StationState } from 'interfaces'
import {
  ApiClient,
  sendEventToWukos,
} from 'modules/data'
import { DashboardService } from 'services'

// noinspection PointlessArithmeticExpressionJS
const AUTO_UPDATE_TIMEOUT = 1 * 60 * 1000

class DashboardStore {
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

  constructor(
    /**
     * @deprecated use DashboardService
     */
    private apiClient: ApiClient,
    private dashboardService: DashboardService,
    ) {
  }

  async reloadData(): Promise<void> {
    this.setLoading(true)
    let [networkEntries, events, enrichedStations, fields, networkSpecialEvents] = await Promise.all([
      this.apiClient.fetchEntries(this.selectedDate),
      this.apiClient.fetchEvents(this.selectedDate),
      this.dashboardService.getStationsAndInfo(),
      this.apiClient.fetchFields(),
      this.apiClient.fetchSpecialEvents(this.selectedDate),
    ])

    const { stations, stationMap } = enrichedStations
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

  stationState(id: string): StationState {
    const entries = this.entries.get(id)

    if (!entries || entries.length === 0) {
      return StationState.missing
    }

    return entries.filter(e => !e.state).length === 0 ? StationState.okay : StationState.notOkay
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

export default DashboardStore
