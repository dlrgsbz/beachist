package de.tjarksaul.wachmanager.dtos

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: String,
    val name: String
) {
    override fun toString(): String {
        return name
    }
}