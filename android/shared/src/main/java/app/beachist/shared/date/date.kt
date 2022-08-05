package app.beachist.shared.date

import java.util.*

fun formatDateTime(date: Date, timeZone: TimeZone? = null): String? {
    val formatter = DateFormatProvider().getShortDateTimeFormat()

    formatter.timeZone = timeZone ?: TimeZone.getDefault()

    return formatter.format(date)
}
