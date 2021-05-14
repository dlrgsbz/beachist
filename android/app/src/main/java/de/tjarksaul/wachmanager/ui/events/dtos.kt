package de.tjarksaul.wachmanager.ui.events


import de.tjarksaul.wachmanager.dtos.EventType
import de.tjarksaul.wachmanager.dtos.NetworkState
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class PostEvent(
    val type: EventType,
    val id: String,
    val date: String
)

data class Event(
    val type: EventType,
    val id: String,
    val date: Date,
    var state: NetworkState
) {
    val dateString: String
        get() {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            return dateFormat.format(date)
        }
}

