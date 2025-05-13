import { ProvisioningRequest } from 'dtos'
import { MqttMessage } from '../../dtos/mqtt'

export interface ApiProvisioningRequest extends Omit<ProvisioningRequest, 'expiresAt'> {
  expiresAt: string
}

export interface ApiStationInfo {
  online: boolean
  date: string
  version: string
  versionCode?: number
}

export interface ApiMqttMessage extends Omit<MqttMessage, 'ts'> {
  ts: string
}

export interface Paginated<T> {
  next: string | undefined
  previous: string | undefined
  results: T[]
}
