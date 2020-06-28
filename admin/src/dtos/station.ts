export interface StationInfo {
  id: string
  name: string
}

export interface Station extends StationInfo {
  fields: Field[]
}

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
