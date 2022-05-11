import { Moment } from 'moment'

export interface ProvisioningRequest {
  id: number
  station: string // uuid
  password: string
  expiresAt: Moment
}

export type ProvisioningRequestMap = Record<string, ProvisioningRequest>
