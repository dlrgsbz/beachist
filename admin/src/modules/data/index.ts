import moment from 'moment'
import {
  EventEntry,
  Field,
  NetworkEntry,
  NetworkSpecialEvent,
  SpecialEvent,
  StationInfo,
  UserInfo,
  UserInfoWithToken,
} from 'dtos'
import { AccessTokenNotFound, AuthService } from 'context/AuthServiceContext'
import axios from 'axios'
import { removeTokens } from 'lib/token'

export interface HttpHeaders {
  [header: string]: string
}

export interface HttpOptions {
  headers?: HttpHeaders
  params?: object
  body?: object
}

export interface HttpResponse<T> {
  data: T
  status: number
}

export class ApiClient {
  constructor(private authService: AuthService) {
  }

  private async request<T>(url: string, method: 'POST' | 'GET', options?: HttpOptions): Promise<HttpResponse<T>> {
    let token = ''
    try {
      token = this.authService.getAndValidateToken()
    } catch (e) {
      if (e instanceof AccessTokenNotFound) {
        removeTokens()
        window.location.replace('/')
      }
    }

    const headers: Record<string, string> = {
      ...options?.headers,
      Authorization: `Bearer ${token}`,
    }

    const requestOptions = {
      params: options?.params,
      headers,
      method,
      url,
    }

    try {
      const response = await axios.request(requestOptions)
      return { data: response.data, status: response.status }
    } catch (error) {
      throw error
    }
  }

  private async get<T>(url: string, options?: HttpOptions): Promise<HttpResponse<T>> {
    return this.request(url, 'GET', options)
  }

  private async post<T>(url: string, data?: object, options?: HttpOptions): Promise<HttpResponse<T>> {
    const requestOptions = {
      ...options,
      body: data,
    }
    return this.request(url, 'GET', requestOptions)
  }

  public async fetchStations(): Promise<StationInfo[]> {
    const data = await this.get<StationInfo[]>('/api/station')
    return data.data
  }

  public async fetchFields(): Promise<Field[]> {
    const data = await this.get<Field[]>('/api/field')
    return data.data
  }

  public async fetchEntries(date: moment.Moment): Promise<NetworkEntry[]> {
    const dateString = date.format('Y-MM-DD')
    const data = await this.get<NetworkEntry[]>(`/api/entry/${dateString}`)
    return data.data.map((dt: any) => ({ ...dt, date: moment(dt.date) }))
  }

  public async fetchEvents(date: moment.Moment): Promise<EventEntry> {
    const dateString = date.format('Y-MM-DD')
    const data = await this.get<EventEntry>(`/api/event/${dateString}`)
    return { ...data.data, date: moment(data.data.date) }
  }

  public async fetchSpecialEvents(date: moment.Moment): Promise<NetworkSpecialEvent[]> {
    const dateString = date.format('Y-MM-DD')
    const data = await this.get<NetworkSpecialEvent[]>(`/api/special/${dateString}`)
    return data.data.map((dt: any) => ({ ...dt, date: moment(dt.date) }))
  }

  public async fetchLoginToken() {
    const data = await this.get<{ token: string }>('/auth/link')
    return data.data.token
  }
}

export async function getUserData(token: string): Promise<UserInfo> {
  try {
    const response = await axios.get('/api/me', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    return response.data
  } catch (error) {
    throw error
  }
}

export async function fetchAvailableUsers(): Promise<UserInfo[]> {
  try {
    const response = await axios.get('/auth/users')
    return response.data
  } catch (error) {
    throw error
  }
}

export async function login(name: string, password: string): Promise<UserInfoWithToken> {
  try {
    const response = await axios.post('/auth/login', { username: name, password })
    return response.data
  } catch (error) {
    throw error
  }
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
