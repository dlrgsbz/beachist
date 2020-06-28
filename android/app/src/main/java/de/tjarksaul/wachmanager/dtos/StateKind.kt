package de.tjarksaul.wachmanager.dtos

import kotlinx.serialization.Serializable

@Serializable
enum class StateKind {
    broken, tooLittle, other
}