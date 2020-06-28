import moment from 'moment'
import { Field, Station } from './station'

export interface BaseEntry {
  id: string
  state: boolean
  stateKind?: StateKind
  amount?: number
  note?: string
  date: moment.Moment
}

export interface NetworkEntry extends BaseEntry {
  field: string
  station: string
}

export enum StateKind {
  broken = 'broken',
  tooLittle = 'tooLittle',
  other = 'other',
}

export interface EventEntry {
  date: moment.Moment
  firstAid: number
  search: number
}

export interface Entry extends BaseEntry {
  station: Station
  field: Field
}
