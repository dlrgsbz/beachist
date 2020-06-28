package de.tjarksaul.wachmanager.dtos

import kotlinx.serialization.Serializable

@Serializable
data class EventStats(
    val firstAid: Int,
    val search: Int,
    val date: String // todo: date
)

