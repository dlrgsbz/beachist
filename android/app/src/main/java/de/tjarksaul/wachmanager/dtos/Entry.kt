package de.tjarksaul.wachmanager.dtos

import kotlinx.serialization.Serializable

@Serializable
data class Entry(
    val id: String?,
    val field: String,
    val station: String,
    val state: Boolean?,
    val stateKind: StateKind?,
    val amount: Int?,
    val note: String?,
    val date: String // todo: date
)

