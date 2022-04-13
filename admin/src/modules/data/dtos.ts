import { ProvisioningRequest } from 'dtos'

export interface ApiProvisioningRequest extends Omit<ProvisioningRequest, 'expiresAt'> {
  expiresAt: string
}

export interface ApiStationInfo {
  online: boolean
  date: string
  version: string
  versionCode?: number
}