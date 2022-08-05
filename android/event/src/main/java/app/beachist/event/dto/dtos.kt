package app.beachist.event.dto


import app.beachist.shared.NetworkState
import kotlinx.serialization.Serializable
import java.util.*

data class Event(
    val type: EventType,
    val id: String,
    val date: Date,
    var state: NetworkState,
)
