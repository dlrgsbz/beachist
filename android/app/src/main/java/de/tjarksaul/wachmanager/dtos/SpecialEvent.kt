package de.tjarksaul.wachmanager.dtos

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
