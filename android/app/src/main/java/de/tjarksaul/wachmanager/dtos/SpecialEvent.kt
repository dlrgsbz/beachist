package de.tjarksaul.wachmanager.dtos

import android.text.format.DateUtils
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class SpecialEvent(
    val id: String,
    val title: String,
    val note: String,
    val notifier: String,
    val date: String, // todo: date
    val kind: SpecialEventKind,
    val networkState: NetworkState = NetworkState.pending
) {
    val actualDate: Date
        get() {
            val dt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(date)
            return Date.from(Instant.from(dt))
        }

    val printableDate: String
        get() {
            val format = if (DateUtils.isToday(actualDate.time)) "HH:mm" else "dd.mm.YYYY HH:mm"
            val df = SimpleDateFormat(format)
            return df.format(actualDate)
        }
}
