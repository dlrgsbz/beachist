package de.tjarksaul.wachmanager.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PostEntry(
    val state: Boolean,
    val stateKind: StateKind?,
    val amount: Int?,
    val note: String?,
    val crew: String
)

