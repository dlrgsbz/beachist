package app.beachist.special_event.dtos

import app.beachist.shared.NetworkState
import java.util.*

data class SpecialEvent(
    val id: String,
    val title: String,
    val note: String,
    val notifier: String,
    val date: Date,
    val kind: SpecialEventKind,
    val networkState: NetworkState = NetworkState.pending,
)
