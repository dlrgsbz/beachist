import { AdminView, StationState } from 'interfaces'
import { ApiClient, sendEventToWukos } from 'modules/data'
import {
  Entry, EventEntry,
  Field,
  NetworkEntry,
  NetworkSpecialEvent,
  SpecialEvent, SpecialEventMap,
  SpecialEventType,
  StationInfo,
  StationInfoMap,
} from 'dtos'
import { action, computed, observable, runInAction } from 'mobx'
import moment, { Moment } from 'moment'

import { DashboardService } from 'services'
import { AsyncState, createAsyncState, runWithAsyncState } from '../../lib'
import { create } from '@mui/material/styles/createTransitions'

// noinspection PointlessArithmeticExpressionJS
const AUTO_UPDATE_TIMEOUT = 1 * 60 * 1000

class DashboardStore {
  @observable selectedDate: Moment = moment('2025-05-04')
  @observable feldListe = new Map<string, string>()
  @observable felder = new Map<string, Field>()
  @observable stations: AsyncState<StationInfo[]> = createAsyncState<StationInfo[]>([])
  @observable events: AsyncState<EventEntry | undefined> = createAsyncState(undefined)
  @observable fields: Field[] = []
  @observable entries = createAsyncState(new Map<string, Entry[]>())
  @observable crews = createAsyncState(new Map<string, string>())
  @observable specialEvents: AsyncState<SpecialEventMap> = createAsyncState({ special: [], damage: [] })
  @observable stationOnlineState: AsyncState<StationInfoMap> = createAsyncState({})

  @observable autoUpdateEnabled = true
  @observable view: AdminView = AdminView.stations

  private timeout: number | undefined

  constructor(
    /**
     * @deprecated use DashboardService
     */
    private apiClient: ApiClient,
    private dashboardService: DashboardService,
  ) {}

  @action.bound
  async reloadData(): Promise<void> {
    // this.setLoading(true)
    await Promise.all([
      runWithAsyncState(this.stations, async () => this.dashboardService.getStations()),
      runWithAsyncState(this.crews, async () => this.dashboardService.getCrewInfo(this.selectedDate)),
      runWithAsyncState(this.entries, async () => this.dashboardService.getEntries(this.selectedDate)),
      runWithAsyncState(this.stationOnlineState, async () => this.dashboardService.getStationInfo()),
      runWithAsyncState(this.events, async () => this.dashboardService.getEvents(this.selectedDate)),
      runWithAsyncState(this.specialEvents, async () => this.dashboardService.getSpecialEvents(this.selectedDate))
    ])

    runInAction(() => {
      this.view = AdminView.stations
    })

    if (this.autoUpdateEnabled) {
      this.timeout = window.setTimeout(() => this.reloadData(), AUTO_UPDATE_TIMEOUT)
    }
  }

  @computed
  get firstAid() {
    return this.events.data?.firstAid ?? 0
  }

  @computed
  get search() {
    return this.events.data?.search ?? 0
  }

  @action.bound
  async loadStationInfo(): Promise<void> {
    const infos = await this.dashboardService.getStationInfo()
    
    const stations = this.stations.data.map(station => {
      const info = infos[station.id]
      if (!info) { return station }
      return {
        ...station,
        ...info,
      }
    })

    runInAction(() => {
      this.stations.data = stations
    })
  }

  @action.bound
  changeSelectedDate(date: Moment | null): void {
    if (date) {
      this.selectedDate = date
      this.reloadData()
    }
  }

  @action.bound
  stationState(id: string): StationState {
    const entries = this.entries.data.get(id)

    if (!entries || entries.length === 0) {
      return StationState.missing
    }

    return entries.filter(e => !e.state).length === 0 ? StationState.okay : StationState.notOkay
  }

  @action.bound
  stationEntries(id: string): Entry[] {
    return this.entries.data.get(id) || []
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

export default DashboardStore
