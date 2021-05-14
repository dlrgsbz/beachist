package de.tjarksaul.wachmanager.repositories.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.tjarksaul.wachmanager.dtos.EventType
import de.tjarksaul.wachmanager.dtos.NetworkState
import java.util.*

@Entity
class EventEntity {
    @PrimaryKey
    var id: String = ""

    var type: EventType = EventType.firstAid
    var date: Date = Date()
    var state: NetworkState = NetworkState.pending
}
