import { formatISO, parseISO } from 'date-fns'

export const formatIso8601Date = (date: Date): string => formatISO(date, { representation: 'date' })

export const parseIso8601Date = (date: string): Date => parseISO(date)
