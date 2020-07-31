import moment from 'moment'
import { EventEntry, Field, NetworkEntry, NetworkSpecialEvent, SpecialEvent, StationInfo } from 'dtos'
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

export async function fetchSpecialEvents(date: moment.Moment): Promise<NetworkSpecialEvent[]> {
  const dateString = date.format('Y-MM-DD')
  const data = await httpGet(`/api/special/${dateString}`)
  return data.data.map((dt: any) => ({ ...dt, date: moment(dt.date) }))
}

export function sendEventToWukos(event: SpecialEvent): void {
  const form = document.createElement('form')
  form.name = 'damage-form'
  form.method = 'POST'
  form.action = 'https://wukos.de/sh/ostholstein/haffkrug-scharbeutz/index.php?p=m_problem'
  form.target = '_blank'

  const text = event.note
  const title = `${event.title} (${event.station.name})`

  const fields = new Map<string, string>([
    ['quelle', ''],
    ['show', ''],
    ['mat_id', '0'],
    ['klasse', '0'],
    ['aktion', 'erstellen'],
    ['titel', title],
    ['text', text],
  ])
  fields.forEach((val, name) => {
    const field = document.createElement('input')
    field.type = 'text'
    field.name = name
    field.value = val
    form.appendChild(field)
  })

  document.body.appendChild(form)
  form.submit()

  document.body.removeChild(form)
}