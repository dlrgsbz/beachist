import { Moment } from 'moment'
import { StationInfo } from './station'

export interface BaseSpecialEvent {
  id: string
  title: string
  note: string
  type: SpecialEventType
  notifier: string
  date: Moment
}

export interface NetworkSpecialEvent extends BaseSpecialEvent {
  station: string
}

export interface SpecialEvent extends BaseSpecialEvent{
  station: StationInfo
}

export enum SpecialEventType {
  event = 'event',
  damage = 'damage',
}
