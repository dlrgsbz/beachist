package app.beachist.special_event.sync

import app.beachist.special_event.dtos.SpecialEventKind
import kotlinx.serialization.Serializable

@Serializable
data class SyncSpecialEvent(
    val id: String,
    val title: String,
    val note: String,
    val notifier: String,
    val date: String,
    val kind: SpecialEventKind,
)
