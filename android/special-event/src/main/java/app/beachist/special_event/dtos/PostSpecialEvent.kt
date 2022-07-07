package app.beachist.special_event.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PostSpecialEvent(val title: String, val note: String, val notifier: String, val type: SpecialEventKind)
