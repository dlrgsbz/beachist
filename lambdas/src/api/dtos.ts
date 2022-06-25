export interface SpecialEvent {
  date?: string | undefined
  note: string
  title: string
  type: 'damage' | 'event'
  notifier: string
}

export interface IdResponse {
  id: string
}

export interface CrewInfo {
  date: string
  crew: string
}
