package de.tjarksaul.wachmanager.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatDateTime(date: Date, timeZone: TimeZone? = null): String? {
    val formatter = SimpleDateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT
    )

    formatter.timeZone = timeZone ?: TimeZone.getDefault()

    return formatter.format(date)
}
