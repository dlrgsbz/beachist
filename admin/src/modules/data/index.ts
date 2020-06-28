import moment from 'moment'
import { EventEntry, Field, NetworkEntry, StationInfo } from 'dtos'
import { httpGet } from 'modules/network'

export async function fetchStations(): Promise<StationInfo[]> {
  const data = await httpGet('/api/station')
  return data.data
}

export async function fetchFields(): Promise<Field[]> {
  const data = await httpGet('/api/field')
  return data.data
}

export async function fetchEntries(date: moment.Moment): Promise<NetworkEntry[]> {
  const dateString = date.format('Y-MM-DD')
  const data = await httpGet(`/api/entry/${dateString}`)
  return data.data.map((dt: any) => ({ ...dt, date: moment(dt.date) }))
}

export async function fetchEvents(date: moment.Moment): Promise<EventEntry> {
  const dateString = date.format('Y-MM-DD')
  const data = await httpGet(`/api/event/${dateString}`)
  return { ...data.data, date: moment(data.data.date) }
}
