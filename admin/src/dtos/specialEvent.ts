import { BaseStationInfo, StationInfo } from './station'

import { Moment } from 'moment'

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

export interface SpecialEvent extends BaseSpecialEvent {
  station: BaseStationInfo
}

export enum SpecialEventType {
  event = 'event',
  damage = 'damage',
}
