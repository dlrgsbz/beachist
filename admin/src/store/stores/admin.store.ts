import { action, observable, runInAction } from 'mobx'
import moment, { Moment } from 'moment'
import { Entry, EventEntry, Field, NetworkEntry, StationInfo } from 'dtos'
import { Color } from 'interfaces'
import { fetchEntries, fetchEvents, fetchFields, fetchStations } from 'modules/data'

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

  async reloadData(): Promise<void> {
    this.setLoading(true)
    // @ts-ignore
    const [entries, events, stations, fields] = await Promise.all<NetworkEntry[], EventEntry, StationInfo[], Field[]>([
      fetchEntries(this.selectedDate),
      fetchEvents(this.selectedDate),
      fetchStations(),
      fetchFields(),
    ])

    const stationMap = new Map<string, StationInfo>()
    stations.forEach(station => stationMap.set(station.id, station))
    const fieldMap = new Map<string, Field>()
    fields.forEach(field => fieldMap.set(field.id, field))

    const theEntries: Entry[] = entries
      .map(entry => {
        const station = stationMap.get(entry.station)
        const field = fieldMap.get(entry.field)
        if (!station || !field) {
          return undefined
        }
        return { ...entry, station, field }
      })
      .flat()

    const entryMap = new Map<string, Entry[]>()
    theEntries.forEach(entry => {
      let stationEntries = entryMap.get(entry.station.id)
      if (!stationEntries) {
        stationEntries = []
      }
      runInAction(() => {
        if (entry.crew) {
          this.crews.set(entry.station.id, entry.crew)
        }
      })
      stationEntries.push(entry)
      entryMap.set(entry.station.id, stationEntries)
    })

    runInAction(() => {
      this.firstAid = events.firstAid
      this.search = events.search
      this.stations = stations
      this.fields = fields
      this.entries = entryMap
      this.setLoading(false)
    })
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
}

export default AdminStore
