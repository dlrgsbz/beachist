package de.tjarksaul.wachmanager.modules.specialEvents.sync

import de.tjarksaul.wachmanager.dtos.SpecialEventKind
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
