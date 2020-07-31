package de.tjarksaul.wachmanager.dtos

import android.text.format.DateUtils
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
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
            val simpleDateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            return simpleDateFormat.parse(date)!!
        }

    val printableDate: String
        get() {
            val format = if (DateUtils.isToday(actualDate.time)) "HH:mm" else "dd.mm.YYYY HH:mm"
            val df = SimpleDateFormat(format)
            return df.format(actualDate)
        }
}
