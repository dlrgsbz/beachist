import { format, formatISO } from "date-fns";

export const formatIso8601Date = (date: Date): string => format(date, 'yyyy-MM-dd')

export const formatIso8601DateTime = (date: Date): string => formatISO(date, { representation: 'complete' })
