import { config } from '../config'
import { ulid } from 'ulid'

export enum BrnType {
  Event = 'event',
  Entry = 'entry',
  SpecialEvent = 'special-event',
  Station = 'station',
  Field = 'field',
  Crew = 'crew',
}

const brnBase = (type: BrnType) => `brn:${config.STAGE}:${config.TENANT}:${type}/`

export const generateBrn = (type: BrnType): string => `${brnBase(type)}${(ulid().toLowerCase())}`

export const validateBrn = (brn: string, type: BrnType): boolean => {
  const regex = new RegExp(`^${brnBase(type)}[a-hjkmnp-tv-z0-9]{26}$`)
  return regex.test(brn)
}
