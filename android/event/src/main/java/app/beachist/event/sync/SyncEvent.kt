package app.beachist.event.sync

import app.beachist.event.dto.EventType
import kotlinx.serialization.Serializable

@Serializable
data class SyncEvent(
    val type: EventType,
    val id: String,
    val date: String,
)
