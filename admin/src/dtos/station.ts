import { Moment } from 'moment'

export interface StationInfo {
  id: string
  name: string
  online?: boolean
  onlineStateSince?: Moment
  appVersion: string
  appVersionCode?: number
}

export interface Station extends StationInfo {
  fields: Field[]
}

export interface StationInfoData {
  online: boolean
  onlineStateSince: Moment
  appVersion: string
  appVersionCode?: number
}

export type StationInfoMap = Record<string, StationInfoData | null>

export interface Field {
  id: string
  name: string
  parent: string
  required?: number
  note?: string
}

export enum FieldValueBrokenKind {
  Missing = 'missing',
  TooLittle = 'toolittle',
  Other = 'other',
}

export interface FieldValue {
  state: boolean
  stateKind?: FieldValueBrokenKind
  description?: string
}

export interface CrewInfo {
  station: string
  crew: string
  date: Moment
}
