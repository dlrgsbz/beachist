import { action, observable, runInAction } from 'mobx'
import moment, { Moment } from 'moment'
import { Entry, EventEntry, Field, NetworkEntry, NetworkSpecialEvent, SpecialEvent, StationInfo } from 'dtos'
import { Color } from 'interfaces'
import { fetchEntries, fetchEvents, fetchFields, fetchSpecialEvents, fetchStations } from 'modules/data'

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
  @observable specialEvents = new Map<string, SpecialEvent[]>()

  async reloadData(): Promise<void> {
    this.setLoading(true)
    // @ts-ignore
    const [
      networkEntries,
      events,
      stations,
      fields,
      networkSpecialEvents,
    ] = await Promise.all<NetworkEntry[],
      EventEntry,
      StationInfo[],
      Field[],
      NetworkSpecialEvent[]>([
      fetchEntries(this.selectedDate),
      fetchEvents(this.selectedDate),
      fetchStations(),
      fetchFields(),
      fetchSpecialEvents(this.selectedDate),
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
      this.specialEvents = specialEvents
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

function createEntryMap(
  entries: NetworkEntry[],
  stationMap: Map<string, StationInfo>,
  fieldMap: Map<string, Field>,
): { entries: Map<string, Entry[]>; crews: Map<string, string> } {
  const crews = new Map<string, string>()

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

function createSpecialEventMap(specialEvents: NetworkSpecialEvent[], stationMap: Map<string, StationInfo>): Map<string, SpecialEvent[]> {
  const map = new Map<string, SpecialEvent[]>()

  specialEvents.forEach(event => {
    const stationId = event.station
    const station = stationMap.get(stationId)
    if (!station) {
      return
    }

    let events = map.get(stationId)
    if (!events) {
      events = []
    }

    events.push({ ...event, station })
    map.set(stationId, events)
  })

  return map
}

export default AdminStore
