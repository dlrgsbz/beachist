import { Moment } from 'moment'
import { StationInfo } from './station'

export interface BaseSpecialEvent {
  id: string
  note: string
  date: Moment
}

export interface NetworkSpecialEvent extends BaseSpecialEvent {
  station: string
}

export interface SpecialEvent {
  station: StationInfo
}
