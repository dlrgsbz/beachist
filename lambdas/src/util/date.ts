import { format } from "date-fns";

export const formatIso8601Date = (date: Date): string => format(date, 'yyyy-MM-dd')
