package app.beachist.event.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.beachist.event.EventType
import app.beachist.shared.NetworkState
import java.util.*

@Entity
class EventEntity {
    @PrimaryKey
    var id: String = ""

    var type: EventType = EventType.firstAid
    var date: Date = Date()
    var state: NetworkState = NetworkState.pending
}
