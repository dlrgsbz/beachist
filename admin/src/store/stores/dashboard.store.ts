import { AdminView, StationState } from 'interfaces'
import { ApiClient, sendEventToWukos } from 'modules/data'
import { Entry, Field, NetworkEntry, NetworkSpecialEvent, SpecialEvent, SpecialEventType, StationInfo } from 'dtos'
import { action, makeObservable, observable, runInAction } from 'mobx'
import moment, { Moment } from 'moment'

import { DashboardService } from 'services'

// noinspection PointlessArithmeticExpressionJS
const AUTO_UPDATE_TIMEOUT = 1 * 60 * 1000

class DashboardStore {
  // selectedDate is internal state for the store for auto-update to work, *do not* use this in the UI
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

  @observable autoUpdateEnabled = true
  @observable view: AdminView = AdminView.stations

  private timeout: number | undefined

  constructor(
    /**
     * @deprecated use DashboardService
     */
    private apiClient: ApiClient,
    private dashboardService: DashboardService,
  ) {
    makeObservable(this)
  }

  @action.bound
  async reloadData(selectedDate: moment.Moment): Promise<void> {
    window.clearTimeout(this.timeout)
    this.setLoading(true)
    this.selectedDate = selectedDate
    const [networkEntries, events, enrichedStations, fields, networkSpecialEvents] = await Promise.all([
      this.apiClient.fetchEntries(selectedDate),
      this.apiClient.fetchEvents(selectedDate),
      this.dashboardService.getStationsWithCrew(selectedDate),
      this.apiClient.fetchFields(),
      this.apiClient.fetchSpecialEvents(selectedDate),
    ])
    // this is purposefully not awaited as it's very long running and shouldn't
    //  have an influence on this.loading
    //  also it's not critical if it fails
    this.dispatchFetchStationInfo()

    const { stations, crews } = enrichedStations
    const fieldMap = new Map<string, Field>()
    fields.forEach(field => fieldMap.set(field.id, field))

    const entries = createEntryMap(networkEntries, fieldMap)

    const specialEvents = createSpecialEventMap(networkSpecialEvents)

    runInAction(() => {
      this.firstAid = events.firstAid
      this.search = events.search
      if (this.stations.length === 0) {
        // might already be set by `dispatchFetchStationInfo`
        this.stations = stations
      }
      this.fields = fields
      this.entries = entries
      this.crews = crews
      this.specialEvents = specialEvents.special
      this.damages = specialEvents.damage
      this.view = AdminView.stations
      this.setLoading(false)
    })

    if (this.autoUpdateEnabled) {
      this.timeout = window.setTimeout(() => this.reloadData(this.selectedDate), AUTO_UPDATE_TIMEOUT)
    }
  }

  @action
  setLoading(loading: boolean): void {
    this.loading = loading
  }

  @action.bound
  async dispatchFetchStationInfo(): Promise<void> {
    const result = await this.dashboardService.getStationWithInfo()
    const info = Object.values(result).filter(val => val) as StationInfo[]

    runInAction(() => {
      this.stations = info
    })
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
      this.timeout = window.setTimeout(() => this.reloadData(this.selectedDate), AUTO_UPDATE_TIMEOUT)
    } else {
      window.clearTimeout(this.timeout)
      this.timeout = undefined
    }
  }

  sendEventToWukos(event: SpecialEvent): void {
    sendEventToWukos(event)
  }
}

function createEntryMap(entries: NetworkEntry[], fieldMap: Map<string, Field>): Map<string, Entry[]> {
  const theEntries: Entry[] = entries.flatMap(entry => {
    const field = fieldMap.get(entry.field)
    if (!field) {
      return []
    }
    return [{ ...entry, field, station: { id: entry.station } }]
  })

  const entryMap = new Map<string, Entry[]>()
  theEntries.forEach(entry => {
    let stationEntries = entryMap.get(entry.station.id)
    if (!stationEntries) {
      stationEntries = []
    }

    stationEntries.push(entry)
    entryMap.set(entry.station.id, stationEntries)
  })

  return entryMap
}

interface SpecialEventMap {
  special: SpecialEvent[]
  damage: SpecialEvent[]
}

function createSpecialEventMap(specialEvents: NetworkSpecialEvent[]): SpecialEventMap {
  const map: SpecialEventMap = { special: [], damage: [] }

  specialEvents.forEach(event => {
    const stationId = event.station

    const specialEvent = { ...event, station: { id: stationId } }
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
